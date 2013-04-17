(ns twit.model
  (:use [korma db core]))

(java.util.Locale/setDefault java.util.Locale/ENGLISH)
(def env (into {} (System/getenv)))

(def dbhost (get env "OPENSHIFT_POSTGRESQL_DB_HOST" "localhost"))
(def dbport (Integer/parseInt (get env "OPENSHIFT_POSTGRESQL_DB_PORT" "5432")))
(def dbuser (get env "OPENSHIFT_POSTGRESQL_DB_USERNAME" "postgres"))
(def dbpassword (get env "OPENSHIFT_POSTGRESQL_DB_PASSWORD" "postgres"))
(def dbname (get env "OPENSHIFT_POSTGRESQL_DB_NAME" "todo"))

(defdb korma-db (postgres 
    {:user dbuser,
     :password dbpassword,
     :host dbhost,
     :port dbport,
     :db dbname,
     :make-pool? false}))

(defentity users)

(defentity follow)

(defentity messages
  (belongs-to users {:fk :user_id}))

(defn create-user [user]
  (insert users (values user)))

(defn create-message [message]
  (insert messages (values message)))

(defn do-follow [follower-id followed-id]
  (insert 
    follow 
    (values {:follower_id follower-id 
             :followed_id followed-id})))

(defn do-unfollow [follower-id followed-id]
  (delete 
    follow 
    (where (and (= :follower_id follower-id) 
                (= :followed_id followed-id)))))

;; BAD should unify into single call, 
;; can't see a simple way in Korma
(defn select-followed-users [user-id]
  (select
    users
    (where (in :id (subselect 
                     follow
                     (fields :followed_id)
                     (where (= :follower_id user-id)))))))

(defn select-unfollowed-users [user-id]
  (select
    users
    (where (not 
             (or 
               (in :id (subselect 
                         follow
                         (fields :followed_id)
                         (where (= :follower_id user-id))))
               (= :id user-id))))))

(defn select-messages [user-id]
  (select 
    messages
    (with users)
    (fields :messages.body :messages.ts :users.username)
    (where 
      (or (= :user_id user-id)
          (in :user_id (subselect 
                         follow
                         (fields :followed_id)
                         (where (= :follower_id user-id))))))))

(defn check-password [user-name password]
  (first (select
           users
           (fields :id)
           (where (and (= :username user-name)
                       (= :password password))))))
