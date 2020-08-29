# MyExoplayer
Android音视频开发之Exoplayer的应用,使用Google官方提供的新的多媒体播放组件Exoplayer来实现Android系统的视频和音乐的播放。
## 视频播放的实现  
使用Exoplayer提供的内置视频播放控件<com.google.android.exoplayer2.ui.PlayerView/>实现对网络视频的播放，我们可以对其播放界面进行高度的自定义。
## 音乐播放的实现
使用Exoplayer实现音乐的播放，Exoplayer只负责音乐的播放，而音乐播放的逻辑；比如上一首，暂停，播放歌曲的状态则交给MediaSession来处理。  


