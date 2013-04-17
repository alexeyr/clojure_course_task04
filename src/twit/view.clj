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
(l/defragment message-frag message-row  [{:keys [id user_id body ts]}]
  (l/element= :h2) (l/content (str "Author: " user_id ", time: " ts ", ID: " id))
  (l/element= :span) (l/content body))

(l/defragment user-frag user-row [{:keys [id username]} followed]
  (l/element= :span) (l/content username)
  (l/element= :form) (l/attr :action (str "/" (action followed) "/" id))
  (l/element= :input) (l/attr :value (action followed)))

;; Shows a from for message creating
;; Path: /message/new
;(l/defragment message-new-item-frag message-item-edit []
;  (l/id= "close") (l/attr :href "/messages")
;  (l/element= :form) (l/attr :action (str "/post")))

(l/defragment flash-frag [flash]
  (l/id= "flash") (l/content flash))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pages

;(defn show-message-list [message-list]
;  (l/document main-html
;              (l/id= "message-grid")
;              (l/content
;               (for [message message-list]
;                 (message-frag message)))))
;
;
;(defn show-message [message]
;  (l/document main-html
;              (l/id= "message-grid")
;              (l/content
;               (message-item-frag message))))
;
;
;(defn edit-message [message]
;  (l/document main-html
;              (l/id= "message-grid")
;              (l/content
;               (message-edit-item-frag message))))
;
;
;(defn show-new-message []
;  (l/document main-html
;              (l/id= "message-grid")
;              (l/content
;               (message-new-item-frag))))

(defn show-login [flash]
  (l/document 
    login-html
    (flash-frag flash)))

;; TODO other fragments!
(defn show-timeline [followed-users unfollowed-users messages flash]
  (l/document 
    main-html
    (flash-frag flash)
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
