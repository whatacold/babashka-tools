My collection of [Babashka](https://book.babashka.org/) tools, including:

- password-generator.clj, a random password generator
- illustrate.clj, to illustrate Clojure snippets

# illustrate.clj

  The illustrated code could be used in websites like https://clojuredocs.org, or included in your blog posts easily.

  A quick diff of the snippet file `foo.clj` and the illustrated counterpart:

  ```diff
  --- foo.clj   2021-08-07 11:47:35.562831545 +0800
  +++ foo.new.clj   2021-08-07 11:47:46.544740020 +0800
  @@ -1,11 +1,15 @@
   (+ 1 2)
  +;; => 3

   (def greeting "hello world!")
  +;; => #'user/greeting

   ;; preverse this dummy comment

   (defn my-sum
     [a b]
     (+ a b))
  +;; => #'user/my-sum

   (my-sum 2 3)
  +;; => 5
  ```

  3 ways to use it:
  1. `illustrate.clj -i .new foo.clj` will illustrate `foo.clj` and write the result to `foo.new.clj`.
  2. `illustrate.clj foo.clj` to illustrate `foo.clj` and **overwrite** it.

     Be careful! You'd better backup your the file or put it under the control of git.

  3. `cat foo.clj | illustrate.clj` to do it via pipe, this could be handy if you use it with tools like Emacs/Vim.
