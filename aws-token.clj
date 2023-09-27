#!/usr/bin/env bb

(require '[babashka.process :refer [sh]])
(require '[clojure.string :refer [join]])

;; parse varaibles 
(def profile (System/getenv "AWS_PROFILE"))
(def serial-number (System/getenv "AWS_SERIAL_NUMBER"))

;; comand to call
(def aws-command "aws sts get-session-token --serial-number %s --token-code %s --profile %s ")

(defn exec-cmd [args]
  (sh (join " " args)))

(defn call-aws-configure-set [key value]
  (exec-cmd ["aws configure set" key value  "--profile temp"]))

(defn save-temp-profile [response]
  (call-aws-configure-set "aws_access_key_id" (get-in response [:Credentials :AccessKeyId]))
  (call-aws-configure-set "aws_secret_access_key" (get-in response [:Credentials :SecretAccessKey]))
  (call-aws-configure-set "aws_session_token" (get-in response [:Credentials :SessionToken]))
  (prn "Done!"))

(defn call-aws [token]
  (sh (format aws-command serial-number token profile)))

(defn get-token [token]
  (let [response (call-aws token)]

    (if-not (str/blank? (:out response))
      (save-temp-profile (json/decode (:out response) true))
      (prn (:err response)))))


(let [[token] *command-line-args*]
  (if (empty? token)
    (prn "token missing")
    (get-token token)))
    
