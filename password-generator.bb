#!/usr/bin/env bb

(import [java.security SecureRandom])

(def ^:private rng (SecureRandom.))

(let [len 16
      passwd (char-array len)
      chars (map char (concat (range 48 58) ; 0-9
                              (range 65 91) ; A-Z
                              (range 97 123) ; a-z
                              [\~ \! \@ \# \$ \% \^ \& \* \( \) \- \+ \=]))
      nchars (count chars)]
  (println (clojure.string/join ""
                                (mapcat (fn [char]
                                          (str (nth chars (.nextInt rng nchars))))
                                        passwd))))
