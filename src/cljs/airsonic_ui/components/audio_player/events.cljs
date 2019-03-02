(ns airsonic-ui.components.audio-player.events
  (:require [re-frame.core :as rf]
            [airsonic-ui.audio.playlist :as playlist]
            [airsonic-ui.api.helpers :as api]))

(rf/reg-event-fx
 ; sets up the db, starts to play a song and adds the rest to a playlist
 :audio-player/play-all
 (fn [{:keys [db]} [_ songs start-idx]]
   (let [playlist (-> (playlist/->playlist songs :playback-mode :linear :repeat-mode :repeat-all)
                      (playlist/set-current-song start-idx))]
     {:audio/play (api/stream-url (:credentials db) (playlist/peek playlist))
      :db (assoc-in db [:audio :playlist] playlist)})))

(rf/reg-event-db
 :audio-player/set-playback-mode
 (fn [db [_ playback-mode]]
   (update-in db [:audio :playlist] #(playlist/set-playback-mode % playback-mode))))

(rf/reg-event-db
 :audio-player/set-repeat-mode
 (fn [db [_ repeat-mode]]
   (update-in db [:audio :playlist] #(playlist/set-repeat-mode % repeat-mode))))

(rf/reg-event-fx
 :audio-player/next-song
 (fn [{:keys [db]} _]
   (let [db (update-in db [:audio :playlist] playlist/next-song)
         next (playlist/peek (get-in db [:audio :playlist]))]
     {:db db
      :audio/play (api/stream-url (:credentials db) next)})))

(rf/reg-event-fx
 :audio-player/previous-song
 (fn [{:keys [db]} _]
   (let [db (update-in db [:audio :playlist] playlist/previous-song)
         prev (playlist/peek (get-in db [:audio :playlist]))]
     {:db db
      :audio/play (api/stream-url (:credentials db) prev)})))

(rf/reg-event-db
 :audio-player/enqueue-next
 (fn [db [_ song]]
   (update-in db [:audio :playlist] #(playlist/enqueue-next % song))))

(rf/reg-event-db
 :audio-player/enqueue-last
 (fn [db [_ song]]
   (update-in db [:audio :playlist] #(playlist/enqueue-last % song))))

(rf/reg-event-fx
 :audio-player/toggle-play-pause
 (fn [_ _]
   {:audio/toggle-play-pause nil}))

(defn audio-update
  "Reacts to audio events fired by the HTML5 audio player and plays the next
  track if necessary."
  [{:keys [db]} [_ status]]
  (cond-> {:db (assoc-in db [:audio :playback-status] status)}
    (:ended? status) (assoc :dispatch [:audio-player/next-song])))

(rf/reg-event-fx :audio/update audio-update)

(rf/reg-event-fx
 :audio-player/seek
 (fn [{:keys [db]} [_ percentage]]
   (let [duration (:duration (playlist/peek (get-in db [:audio :playlist])))]
     {:audio/seek [percentage duration]})))

(rf/reg-event-fx
 :audio-player/set-volume
 (fn [_ [_ percentage]]
   {:audio/set-volume percentage}))

(rf/reg-event-fx
 :audio-player/increase-volume
 (fn [_ _]
   {:audio/increase-volume nil}))

(rf/reg-event-fx
 :audio-player/decrease-volume
 (fn [_ _]
   {:audio/decrease-volume nil}))
