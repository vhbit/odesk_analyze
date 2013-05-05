(ns odesk_analyze.core
  (:require [clj-http.client :as client])
  (:require [clojure.string :as string])
  (:require [odesk_analyze.api :as odesk])
  (:require [odesk_analyze.cache :as cache])
  (:require [odesk_analyze.date :as dates])
  (:require [odesk_analyze.path :as path])

  (:use [odesk_analyze.auth :only [+file-auth-storage]])
  (:use [odesk_analyze.utils :only [read-edn]])
  (:use [clojure.java.io :only [file]])

  (:import (java.util Date))
  (:import (java.text SimpleDateFormat))

  (:gen-class :main true))

(defn get-work-diary [api date config]
  (let [username (:username config)
        company-name (:company config)
        company-id (odesk/get-team-id api company-name)
        formatted-date (.format (SimpleDateFormat. "yyyyMMdd") date)
        today (Date.)
        cache-key (format "%s@%s-%s" username company-name formatted-date)
        cache (cache/+file-cache (or (:cache-dir config nil) "~/.odesk_stats"))]
    (or
      (.get-value cache cache-key)
      (try
        (let [results (->> (odesk/get-workdiary api company-id username date)
                           (:snapshot)
                           (map :memo))]
          (if-not (dates/same-day today date)
            (.set-value cache cache-key results))
          results)
        (catch Exception e (do
                             (println "Retrying for" formatted-date)
                             (Thread/sleep 2)
                             (get-work-diary api date config)))))))

(defn tasks-from-diary [diary]
  (let [extract-prefix (fn [x] (-> (re-find #"\[?([a-zA-Z]+)]?" x)
                                   (nth 1)
                                   (or x)))]
    (->>
     (filter (complement empty?)  diary)
     (map #(string/lower-case (extract-prefix %)))
     (frequencies))))

(defn diary-from-dates [api dates config]
  (reduce concat (pmap #(get-work-diary api % config) dates)))

(defn frob-delegate [data]
    (print "Please continue after log in through" (:url data))
    (flush)
    (read-line))

(defn main [args]
  (let [config (read-edn (path/expand "~/.odeskrc"))
        auth-storage (+file-auth-storage (path/expand "~/.odesk_auth"))
        api (odesk/+client (:secret config)
                           (:api-key config)
                           frob-delegate
                           auth-storage)
        dates (apply dates/dates-widest-interval (dates/dates-from-args args))
        date-seq (apply dates/dates-between dates)
        diary (diary-from-dates api date-seq config)]
     (println (format "Work diary since %s till %s " (first dates) (second dates)))
     (doseq [item (map #(format "%s = %s min" (key %) (* 10 (val %))) (tasks-from-diary diary))]
        (println item)))
     (println "Done. Have a nice day!"))

(defn -main [& args]
  (println "Gathering data...")
  (main args)
  (System/exit 0))
