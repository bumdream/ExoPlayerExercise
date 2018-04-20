## GifExtractor with Exoplayer 

<p >
	<img src="output.gif", width="200">
	<img src="screenshot.jpeg", width="200">

</p>

### Logic

1. cut the video from startPos to endPos.
2. calculate the count of frames we need with FPS and save the frame locations onto the array.
3. Extract the frame from each location and add it to gif encoder.
4. make gif file.

### Features

-  use TextureView to grab frames at specific position.
-  use VideoListener to prevent seekTo() method from being ignored.

### Todo list

1. change TextureView to SurfaceView
2. custom controlview
3. UI design