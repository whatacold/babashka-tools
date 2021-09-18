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
  (if (clojure.string/blank? src)
    src
    (let [zloc (z/of-string src)]
      (-> zloc
          remove-illustration-comments
          add-illustration-comments
          z/root-string))))

(defn illustrate-org-file
  "Add illustration comments to an org-mode file"
  [file new-file]
  (let [lines (clojure.string/split-lines (slurp file))
        result (reduce (fn
                         [state line]
                         (let [content (nth state 0)
                               prev-in-block? (nth state 1)
                               src-block (nth state 2)]
                           (if prev-in-block?
                             (if (re-matches #"\s*#\+end_src" line)
                               [(str content (illustrate-string src-block) line "\n")
                                false
                                ""]
                               [content
                                true
                                (str src-block line "\n")]) ; append to the source block
                             ;; not in a src block previously
                             [(str content line "\n")
                              (if (re-matches #"\s*#\+begin_src\s+clojure" line)
                                true
                                false)
                              ""])))
                       ["" false ""]
                       lines)]
    (spit new-file (nth result 0))))

(defn illustrate-file
  "Illustrate the top-level forms in file, and write the result back to the file with suffix"
  [file suffix]
  (let [new-file (clojure.string/replace file
                                         #"(\.[a-z]+)$"
                                         (str suffix "$1"))]
    (if (clojure.string/ends-with? file ".org")
      (illustrate-org-file file new-file)
      (spit new-file (illustrate-string (slurp file))))))

;; main
(let [files (:arguments opts)]
  (if (empty? files)
    (println (illustrate-string (slurp *in*)))
    (illustrate-file (first files) (:in-place (:options opts)))))
