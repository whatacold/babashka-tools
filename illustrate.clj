#!/usr/bin/env bb

(require '[clojure.tools.cli :refer [parse-opts]])

(def cli-options
  ;; An option with a required argument
  [["-i" "--in-place SUFFIX" "Suffix for new illustrated files"
    :default ""
    :validate [#(<= 1 (count %) 5) "Must be a string having length of between 1 and 5"]]
   ["-h" "--help"]])

(def opts (parse-opts *command-line-args* cli-options))

(defn illustrate
  [file suffix]
  ;; (println "file" file "suffix" suffix ".")
  (with-open [in (java.io.PushbackReader. (clojure.java.io/reader
                                           file))]
    (let [edn-seq (repeatedly (partial edn/read {:eof :theend} in))]
      (spit (str file suffix ".x")
            (with-out-str
              (dorun (map (fn [obj]
                            (println)
                            (prn obj)
                            (println "; => " (eval obj)))
                          (take-while (partial not= :theend) edn-seq))))))))

(illustrate (first (:arguments opts)) (:in-place (:options opts)))
