(ns airsonic-ui.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            [airsonic-ui.routes :as routes]
            [airsonic-ui.events :as events]
            [airsonic-ui.subs :as subs]))

;; login form

(defn login-form []
  (let [user (r/atom "")
        pass (r/atom "")]
    (fn []
      [:div
       [:div
        [:span "User"]
        [:input {:type "text"
                 :name "user"
                 :on-change #(reset! user (-> % .-target .-value))}]]
       [:div
        [:span "Password"]
        [:input {:type "password" :name "pass" :on-change #(reset! pass (-> % .-target .-value))}]]
       [:div
        [:button {:on-click #(re-frame/dispatch [::events/authenticate @user @pass])} "Submit"]]])))

;; album list

(defn album-item [album]
  (let [{:keys [artist artistId name coverArt year id]} album]
    [:div
     ;; link to artist page
     [:a {:href (routes/url-for ::routes/artist-view {:id artistId})} artist]
     " - "
     ;; link to album
     [:a {:href (routes/url-for ::routes/album-view {:id id})} name] (when year (str " (" year ")"))]))

(defn album-list []
  (let [albums @(re-frame/subscribe [::subs/current-album-list])]
    [:ul
     (map-indexed (fn [idx album]
                    [:li {:key idx} [album-item album]])
                  albums)]))

;; putting everything together

(defn app [route params query]
  (let [login @(re-frame/subscribe [::subs/login])]
    [:div
     [:h2 (str "Currently logged in as " (:u login))]
     [:h3 (str "Recently played")]
     [album-list]
     [:a {:on-click #(re-frame/dispatch [::events/initialize-db]) :href "#"} "Logout"]]))

(defn main-panel []
  (let [[route params query] @(re-frame/subscribe [::subs/current-route])]
    [:div
     [:h1 "Airsonic"]
     (case route
       ::routes/login [login-form]
       [app route params query])]))