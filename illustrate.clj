#!/usr/bin/env bb

(require '[clojure.tools.cli :refer [parse-opts]]
         '[rewrite-clj.zip :as z]
         '[rewrite-clj.node :as n])

(def cli-options
  ;; An option with a required argument
  [["-i" "--in-place SUFFIX" "Suffix for new illustrated files"
    :default ""
    :validate [#(<= 1 (count %) 5) "Must be a string with length of between 1 and 5"]]
   ["-h" "--help"]])

(def opts (parse-opts *command-line-args* cli-options))

(defn remove-illustration-comments
  "Remove illustration comments like `;; => xxx`."
  [zloc]
  (loop [zloc (-> zloc z/root z/edn* z/next*)] ; rewind to first zloc
    (when zloc
      (let [zloc (if (and (= :comment (z/tag zloc))
                          (re-find #"^;; =>" (z/string zloc))
                          )
                   (z/remove* zloc)
                   zloc)
            right (z/right* zloc)]
        (if-not right
          zloc
          (recur right))))))

(defn add-illustration-comments
  "Add illustration comments like `;; => xxx` for top-level forms."
  [zloc]
  (loop [zloc (-> zloc z/root z/edn)]
    (let [comment (n/comment-node (->> zloc
                                       z/sexpr
                                       eval
                                       (str "; => ")))
          zloc (-> zloc
                   (z/insert-right* comment)
                   (z/insert-right* (n/newlines 1)))
          right (z/right zloc)]
      (if right
        (recur right)
        zloc))))

(defn illustrate-string
  "Illustrate the string, and return the result."
  [src]
  (let [zloc (z/of-string src)]
    (-> zloc
        remove-illustration-comments
        add-illustration-comments
        z/root-string)))

(defn illustrate-file
  "Illustrate the top-level forms in file, and write the result back to the file with suffix"
  [file suffix]
  (spit (clojure.string/replace file #"(\.[a-z]+)$" (str suffix "$1"))
        (illustrate-string (slurp file))))

(let [files (:arguments opts)]
  (if (empty? files)
    (println (illustrate-string (slurp *in*)))
    (illustrate-file (first files) (:in-place (:options opts)))))
