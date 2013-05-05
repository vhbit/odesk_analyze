(ns odesk_analyze.api
  (:require digest)
  (:require [clj-http.client :as http])
  (:import (java.text SimpleDateFormat)))

(defprotocol KeyValueStorage
  (get-value [_ key])
  (set-value [_ key value]))


(def token-check-url "https://www.odesk.com/api/auth/v1/keys/token.json")
(def auth-url-fmt "https://www.odesk.com/services/api/auth/?api_key=%s&frob=%s&api_sig=%s")
(def token-request-url "https://www.odesk.com/api/auth/v1/keys/tokens.json")
(def frob-url "https://www.odesk.com/api/auth/v1/keys/frobs.json")
(def teamrooms-url "https://www.odesk.com/api/team/v2/teamrooms.json")
(def workdiary-url-fmt "https://www.odesk.com/api/team/v1/workdiaries/%s/%s/%s.json")

(declare token)

(defn- collapsed-query [query-map]
  (->> query-map
       (into (sorted-map))
       (map #(str (name (first %)) (last %)))
       (apply str)))

(defn- sig-for-query [api-secret query-map]
  (->> query-map
       (collapsed-query)
       (str api-secret)
       (digest/md5)))

(defn- signed-query [api-secret query-map]
  (assoc query-map :api_sig (sig-for-query api-secret query-map)))

(defn- api-response [client url query-map]
  (http/get url {:query-params (signed-query (:api-secret @client) query-map), :as :json, :accept :json}))

(defn- token-api-response
  ([client url] (token-api-response client url {}))
  ([client url query-map]
   (api-response client
                 url
                 (into query-map
                       {:api_key (:api-key @client)
                        :api_token (token client)}))))

(defn token-is-valid [client token]
  (try
    (-> client
        (api-response token-check-url {:api_key (:api-key @client)
                                                     :api_token token})
        (:status)
        (< 400))
    (catch Exception e
      false)))

(defn- auth-url [client frob]
  (let [sig (sig-for-query (:api-secret @client)
                           {:api_key (:api-key @client)
                            :frob frob})]
    (format auth-url-fmt (:api-key @client) frob sig)))

(defn get-frob [client]
  (let [frob (-> (api-response client frob-url {:api_key (:api-key @client)})
                 (:body)
                 (:frob))]
    (if-not ((:frob-delegate @client) {:frob frob :url (auth-url client frob)})
      (throw (Exception. "Can't do anything without frob authentication confirmed!"))
      (do
        (set-value (:auth-storage @client) :frob frob)
        (swap! client assoc :frob frob)
        frob
        ))))

(defn request-token [client frob]
  (let [result (->> (api-response client token-request-url {:api_key (:api-key @client), :frob frob})
                    (:body)
                    (:token))]
    (set-value (:auth-storage @client) :token result)
    (swap! client assoc :token result)
    result))

(defn token [client]
  (let [auth-storage (:auth-storage @client)
        frob (or (get-value auth-storage :frob) (get-frob client))
        result (or (get-value auth-storage :token) (request-token client frob))]
    (if (token-is-valid client result)
      result
      (request-token))))


(defn get-teamrooms [client]
  (->  client
       (token-api-response teamrooms-url)
       (:body)
       (:teamrooms)))

(defn get-team-id [client team-name]
  (->> client
       (get-teamrooms)
       (:teamroom)
       (filter #(= team-name (:name %)))
       (first)
       (:id)))

(defn get-workdiary [client company-id username date]
  (->> date
      (.format (SimpleDateFormat. "yyyyMMdd"))
      (format workdiary-url-fmt company-id username)
      (token-api-response client)
      (:body)
      (:snapshots)))

(defn +client [api-secret api-key frob-delegate auth-storage]
  (atom {
         :api-secret api-secret
         :api-key api-key
         :frob-delegate frob-delegate
         :auth-storage auth-storage}))
