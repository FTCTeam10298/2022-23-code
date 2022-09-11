"Rect"

- 2d rectangle of coordinates

"TSEPosition"

- Enum for which position

"Regions"

- Rect + TSEPosition
- assigning the rects a name

"Colors"

- giving the rectangles a cool color to look at 

"Position"

- which TSEPosition it's currently in

"Submats"

- Taking a portion of the screen (dimensions are determined by regions variable)
- Regions -> pixels



Detection alg

for each submat check whether it has more yellow than the previous submat. if so this is the rect. if not go on to the next rect