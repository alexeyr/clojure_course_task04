(ns twit.model
  (:use [korma db core]))

(def env (into {} (System/getenv)))

(def dbhost (get env "OPENSHIFT_POSTGRESQL_DB_HOST" "127.0.0.1"))
(def dbport (get env "OPENSHIFT_POSTGRESQL_DB_PORT" 5432))
(def dbuser (get env "OPENSHIFT_POSTGRESQL_DB_USER" "postgresql"))
(def dbpassword (get env "OPENSHIFT_POSTGRESQL_DB_PASSWORD" "postgresql"))
(def dbname (get env "OPENSHIFT_POSTGRESQL_DB_NAME" "twit"))

(defdb korma-db (postgres 
    {:user dbuser,
     :password dbpassword,
     :host dbhost,
     :port dbport,
     :db dbname,
     :make-pool? true}))

(defentity users)

(defentity follow)

(defentity messages
  (belongs-to users {:fk :user_id}))

(defn create-user [user]
  (insert users (values user)))

(defn create-message [message]
  (insert messages (values message)))

(defn follow [follower-id followed-id]
  (insert 
    follow 
    (values {:follower_id follower-id 
             :followed_id followed-id})))

(defn unfollow [follower-id followed-id]
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
    (where 
      (or (= :user_id user-id)
          (in :user_id (subselect 
                         follow
                         (fields :followed_id)
                         (where (= :follower_id user-id))))))))

(defn check-password {user-name :user-name password :password}
  (first (select
           users
           (fields :id)
           (where (and (= :username user-name)
                       (= :password password))))))
