# oksu-station

### Exoplayer 활용하여 옥수역 귀신 웹툰 일부를 모방한 앱입니다.

원작: [2020 호랑 공포 단편선 9화 옥수역 귀신 -Remastered-](https://comic.naver.com/webtoon/detail?titleId=752534&no=9&weekday=tue)


아래 프로젝트 산출 이미지는 공포 기획물 일부를 모방함에 있어 스포일러 혹은 충격적인 장면이 일부 포함되어 있습니다.

<details>
<summary>산출물 보기</summary>
<p>
  
![Alt Text](https://media.giphy.com/media/4zQX3Fc0YJuvvvHS9E/giphy.gif)

</p>
</details>  

#### 구현방법

만화 컷 ImageView 들과 PlayerView 를 포함하는 스크롤뷰로 구현

1. 스크롤을 내리면서 특정 컷(위치) 도달한다.
```kotlin
if (viewBinding.scrollView.scrollY + screenHeight > viewBinding.videoView.top) {
  ...
}
```
2. 손동작 영상을 재생할 PlayerView 전체가 화면에 보일 때까지 스크롤을 내린다. 
```kotlin
viewBinding.scrollView.setOnTouchListener { v, event -> true } // 스크롤 이벤트 막기
ObjectAnimator.ofInt(
    viewBinding.scrollView,
    "scrollY",
    viewBinding.videoView.top
).setDuration(800).start()
```
3. 손동작 영상을 재생한다.
```kotlin
player?.let {
    it.playWhenReady = true
    it.addListener(playbackStateListener) // 영상 종료 시, 스크롤 이벤트 허용하도록 Player EventListener 추가
    it.prepare()
}
```
4. PlayerView 가 보이지 않을 때까지 스크롤을 올리면, 영상을 되돌려 다음 손동작 영상이 반복되도록 준비한다.
```kotlin
player?.let {
    it.removeListener(playbackStateListener)
    it.playWhenReady = false
    it.seekTo(0, 0L)
}
```
