(ns example.core
  (:require [omniconf.core :as cfg])
  (:gen-class))

(defn init-config [cli-args]
  (cfg/define
    {:help {:description "prints this help message"
            :help-name "my-script"
            :help-description "description of the whole script"}
     :boolean-option {:description "can be either true or false"
                      :type :boolean}
     :string-option {:type :string
                     :description "this option's value is taken as is"}
     :integer-option {:type :number
                      :description "parsed as integer"}
     :edn-option {:type :edn
                  :description "read as EDN structure"
                  :default '(1 2)}
     :file-option {:type :file
                   :description "read as filename"}
     :directory-option {:type :file
                        :description "read as directory name"}
     :option-with-default {:parser cfg/parse-number
                           :default 1024
                           :description "has a default value"}
     :required-option {:type :string
                       :required true
                       :description "must have a value before call to `verify`, otherwise fails"}
     :conditional-option {:parser identity
                          :required (fn [] (= (cfg/get :option-with-default) 2048))
                          :description "must have a value if a condition applies"}
     :option-from-set {:type :keyword
                       :one-of #{:foo :bar :baz}
                       :description "value must be a member of the provided list"}
     :existing-file-option {:type :file
                            :verifier cfg/verify-file-exists
                            :description "file should exist"}
     :nonempty-dir-option {:type :directory
                           :verifier cfg/verify-directory-non-empty
                           :description "directory must have files"}
     :delayed-option {:type :number
                      :delayed-transform (fn [v] (+ v 5))
                      :description "has a custom transform that is called the first time the option is read"}
     :renamed-option {:type :boolean
                      :env-name "MY_OPTION"
                      :opt-name "custom-option"
                      :description "has custom names for different sources"}
     :secret-option {:type :string
                     :secret true}
     :conf-file {:type :file
                 :verifier cfg/verify-file-exists
                 :description "it is idiomatic to provide a configuration file just as another option"}
     :nested-option {:description "has child options"
                     :default {:first "alpha"}
                     :nested {:first {:description "nested option one"
                                      :type :string}
                              :second {:description "nested option two"
                                       :type :number
                                       :default 70}
                              :more {:nested {:one {:type :string
                                                    :default "one"}
                                              :two {:type :string}}}}}})

  (cfg/populate-from-cmd cli-args)
  (when-let [conf-file (cfg/get :conf-file)]
    (cfg/populate-from-file conf-file))
  (cfg/populate-from-env)

  (cfg/verify))

(defn run-application []
  (println "Now actually starting the app...")
  (println "Option-from-set is" (cfg/get :option-from-set)))

(defn -main [& args]
  (init-config args)
  (run-application))
