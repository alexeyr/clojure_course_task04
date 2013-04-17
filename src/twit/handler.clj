(ns twit.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [twit.model :as model]
            [twit.view :as view]
            [ring.util.response :as resp]
            [noir.util.middleware :as noir]
            [noir.session :as session]
            ))

(defn redirect-timeline []
  (resp/redirect "/timeline"))

(defn redirect-login []
  (resp/redirect "/login"))

(defn req-logged-in [redirect-timeline? fun]
  (if-let [user-id (session/get :user-id)]
    (if redirect-timeline?
      (do
        (fun user-id)
        (redirect-timeline))
      (fun user-id))
    (redirect-login)))

(defn show-timeline []
  (req-logged-in false
    (fn [id]
      (view/show-timeline
        (model/select-followed-users id)
        (model/select-unfollowed-users id)
        (model/select-messages id)
        (session/flash-get :msg)))))

(defn post [message]
  (req-logged-in 
    true
    (fn [id] (model/create-message (assoc message :user_id id)))))

(defn show-login []
  (view/show-login (session/flash-get :msg)))

(defn follow [followed-id]
  (req-logged-in 
    true
    (fn [id]
      (model/do-follow id followed-id))))

(defn unfollow [followed-id]
  (req-logged-in 
    true
    (fn [id]
      (model/do-unfollow id followed-id))))

(defn login [username password]
  (if-let [{user-id :id} (model/check-password username password)]
    (do
      (session/put! :user-id user-id)
      (redirect-timeline))
    (do
      (session/flash-put! :msg "Failed to login")
      (redirect-login)))
  )

(defn logout []
  (req-logged-in 
    false
    (fn [id]
      (do
        (session/clear!)
        (session/flash-put! :msg "Logged out")
        (redirect-login)))))

(defroutes app-routes
  (GET "/" [] (redirect-timeline))
  ;; Show timeline
  (GET "/timeline" [] (show-timeline))
  ;; show login page
  (GET "/login" [] (show-login))
  ;; Create new message
  (POST "/post" req (post (:params req)))
  (POST "/follow/:id" [id] (follow (Integer/parseInt id)))
  (POST "/unfollow/:id" [id] (unfollow (Integer/parseInt id)))
  (POST "/login" [username password] (login username password))
  (POST "/logout" req (logout))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (->
   [(handler/site app-routes)]
   noir/app-handler
   noir/war-handler
   ))