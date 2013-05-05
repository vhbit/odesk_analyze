(ns odesk_analyze.date
  (:import (java.util Date Calendar GregorianCalendar))
  (:import (java.text SimpleDateFormat))
  (:import (com.joestelmach.natty Parser DateGroup)))

(defn dates-widest-interval [date & other]
  (let [sorted-dates (sort (conj other date))]
    (list (first sorted-dates) (last sorted-dates))))

(defn dates-between
  ([date] (dates-between date (Date.)))
  ([date1 date2]
    (let [cal (GregorianCalendar.)
          result '()]
      (.setTime cal date1)
      (loop [cur-date (.getTime cal)
             result '()]
        (if (.after cur-date date2)
          result
          (do
            (.add cal Calendar/DAY_OF_MONTH 1)
            (recur (.getTime cal) (conj result cur-date))))))))

(defn same-day [date1 date2]
  (let [cal1 (GregorianCalendar.)
        cal2 (GregorianCalendar.)]
    (.setTime cal1 date1)
    (.setTime cal2 date2)
    (and (= (.get cal1 Calendar/DAY_OF_MONTH) (.get cal2 Calendar/DAY_OF_MONTH))
      (= (.get cal1 Calendar/MONTH) (.get cal2 Calendar/MONTH))
      (= (.get cal1 Calendar/YEAR) (.get cal2 Calendar/YEAR)))))

(defn date-from-text [text]
  (let [parser (Parser.)]
    (-> (.parse parser text)
        (nth 0)
        (.getDates)
        (nth 0))))

(defn dates-from-args [args]
  (filter (complement nil?) (map date-from-text args)))
