(ns dk.salza.dired
  "Dired for navigating files.
  Special keys:

    C-j    Parent folder
    Enter  Choose file or navigate to folder

  # To enable
  Add 

    {:deps mogenslund/dired
           {:git/url \"https://github.com/mogenslund/dired.git\"
            :tag \"v0.1.0\"}}

  to deps.edn

  Require [dk.salza.dired :as dired]

  Eval:

    (editor/add-interactive \"dired\" #(dired/run (editor/get-folder)))"
  (:require [dk.salza.liq.editor :as editor]
            [dk.salza.liq.slider :refer :all]
            [dk.salza.liq.extensions.linenavigator]
            [dk.salza.liq.apps.textapp :as textapp]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;; Dired with java io
;; -rw-rw-r--  1 mogens mogens  227 Oct 13 09:26 dired.clj
;; Rename file
;; Search below
;; Make dir

(def state
  (atom {:dir ""
         :entries ()
         :keymap-insert {}
         :keymap-navigation {}
        }))

(defn date-string
  [longtime]
  (str/replace
    (str/replace (str (java.util.Date. longtime))
      #"^\w+ " "")
    #"(?<=:\d\d )\w+ " ""))

(defn file-info-string
  [f]
  (when f
    (str (date-string (.lastModified f)) " "
         (if (.isDirectory f) (str "[" (.getName f) "]") (.getName f)))))

(defn set-dir
  [dir]
  (swap! state assoc :dir dir)
  (swap! state assoc :entries
    (concat (list (.getParentFile dir))
            (sort (filter #(.isDirectory %) (.listFiles dir)))
            (sort (filter #(not (.isDirectory %)) (.listFiles dir))))))

(defn update-display
  []
  (editor/apply-to-slider
    (fn [sl]
      (-> sl
          clear
          (insert (str "# " (@state :dir)))
          insert-newline
          ;(insert "             [a r Rename] [a m New Folder] [a d Delete]")
          ;a c copy file name
          insert-newline
          (insert "                     ")
          (insert (if (.getParentFile (@state :dir)) "[..]" "    "))
          insert-newline
          (insert (str/join "\n" (map file-info-string (rest (@state :entries)))))
          beginning
          (find-next "[")
        )))
   (editor/update-mem-col))

(defn parent
  []
  (set-dir (or (.getParentFile (@state :dir)) (@state :dir)))
  (update-display))

(defn choose
  []
  (let [l (- (get-linenumber (editor/get-slider)) 3)
        f (when (>= l 0) (nth (@state :entries) l))]
    (when f
      (if (.isDirectory f)
        (do (set-dir f)
            (update-display))
        (do (editor/find-file (.getAbsolutePath f)))))))

(defn init-keymaps
  []
  (swap! state assoc
    :keymap-navigation
    (assoc @textapp/keymap-navigation
      "\t" #(editor/set-keymap (@state :keymap-insert))
      "C-j" parent
      "\n" choose)
    :keymap-insert
    (assoc @textapp/keymap-insert
      "\t" #(editor/set-keymap (@state :keymap-navigation))
      "\n" identity)))


(defn run
  [folder]
  (set-dir (io/file (or folder (.getAbsolutePath (io/file "")))))
  (editor/new-buffer "dired")
  (init-keymaps)
  (editor/set-keymap (@state :keymap-navigation))
  (update-display))