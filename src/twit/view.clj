(ns twit.view
  (:require [me.raynes.laser :as l]
            [clojure.java.io :refer [file]]))

(def main-html
  (l/parse
   (slurp (clojure.java.io/resource "public/html/main.html"))))

(def login-html
  (l/parse
   (slurp (clojure.java.io/resource "public/html/login.html"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fragments

(defn row [name] 
  (l/select main-html (l/id= (str name "-row"))))

(def user-row (row "user"))
(def message-row (row "message"))

(defn action [followed]
  (if followed "unfollow" "follow"))

;; Used to show a message in the messages list
;; Path: /messages/
(l/defragment message-frag message-row  [{:keys [username body ts]}]
  (l/element= :h2) (l/content (str "Author: " username ", time: " ts))
  (l/element= :span) (l/content body))

(l/defragment user-frag user-row [{:keys [id username]} followed]
  (l/element= :span) (l/content username)
  (l/element= :form) (l/attr :action (str "/" (action followed) "/" id))
  (l/element= :input) (l/attr :value (action followed)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pages

(defn show-login [flash]
  (l/document 
    login-html
    (l/id= "flash") (l/content flash)))

(defn show-timeline [followed-users unfollowed-users messages flash]
  (l/document 
    main-html
    (l/id= "flash") 
    (l/content flash)
    (l/id= "followed-user-holder")
    (l/content 
      (for [user followed-users]
        (user-frag user true)))
    (l/id= "unfollowed-user-holder")
    (l/content 
      (for [user unfollowed-users]
        (user-frag user false)))
    (l/id= "message-holder")
    (l/content 
      (for [message messages]
        (message-frag message)))))
