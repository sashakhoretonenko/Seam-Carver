Programming Assignment 7: Seam Carving


/* *****************************************************************************
 *  Describe concisely your algorithm to find a horizontal (or vertical)
 *  seam.
 **************************************************************************** */

To find a seam, I maintained an indexed min PQ, that had width * height
indices. To convert from a 2d array to 1d indices, I used the same simple
formula that we used in that one COS 126 assignment, i.e. row 1 is indexed
0 through width - 1, and then row 2 is indexed width through 2 * width - 1, etc.
Simultaeously, I created a 2d array that stored the respective distances of the
shortest path to every pixel. I made sure to initialize every element in this
2d array to -1 to account for the corner case that the length of a path to a
point is 0 when I'm updating the distance. Then I inserted the first column
(for a horizontal seam) and the first row (for a vertical seam) into the pq
with their respective distances being the energy of that pixel. I also created a
2d boolean array so I could check whether a pixel had been dequeued from the
array, which would mean that the shortest path to it was already found so
we can not waste time relaxing it. This ensures that my algorithm won't go
into a permanent loop. I then used Djikstra's algorithm to find the shortest
seam, which terminated once it found a path reaching to the opposite edge of
the image. Oh I also maintained a pixelTo array that kept track of which where
each pixel pointed so I could compute the sequence of pixels that made up
the shortest path at the end.


/* *****************************************************************************
 *  Describe what makes an image suitable to the seam-carving approach
 *  (in terms of preserving the content and structure of the original
 *  image, without introducing visual artifacts). Describe an image that
 *  would not work well.
 **************************************************************************** */

An image that is particularly detailed with lots of variation would be suitable
since the shortest path would be relatively obvious. Black and white images,
or images that depend on patterns of the same color would not work well because
the algorithm would end up removing important chunks of thie image. An example
of an image that would not work well is stripes.png.


/* *****************************************************************************
 *  Perform computational experiments to estimate the running time to reduce
 *  a W-by-H image by one column and one row (i.e., one call each to
 *  findVerticalSeam(), removeVerticalSeam(), findHorizontalSeam(), and
 *  removeHorizontalSeam()). Use a "doubling" hypothesis, where you
 *  successively increase either W or H by a constant multiplicative
 *  factor (not necessarily 2).
 *
 *  To do so, fill in the two tables below. Each table must have 5-10
 *  data points, ranging in time from around 0.25 seconds for the smallest
 *  data point to around 30 seconds for the largest one.
 **************************************************************************** */

(keep W constant)
 W = 2000
 multiplicative factor (for H) =

 H           time (seconds)      ratio       log ratio
------------------------------------------------------
250             0.539           null        null
500             0.949           1.761       0.816
1000            1.572           1.656       0.728
2000            3.178           2.022       1.015
4000            4.673           1.470       0.556
8000            9.043           1.935       0.952


(keep H constant)
 H = 2000
 multiplicative factor (for W) =

 W           time (seconds)      ratio       log ratio
------------------------------------------------------
250             0.644           null        null
500             0.821           1.275       0.350
1000            1.122           1.367       0.451
2000            1.302           1.160       0.215
4000            3.44            2.642       1.402
8000            11.137          3.238       1.695



/* *****************************************************************************
 *  Using the empirical data from the above two tables, give a formula
 *  (using tilde notation) for the running time (in seconds) as a function
 *  of both W and H, such as
 *
 *       ~ 5.3*10^-8 * W^5.1 * H^1.5
 *
 *  Briefly explain how you determined the formula for the running time.
 *  Recall that with tilde notation, you include both the coefficient
 *  and exponents of the leading term (but not lower-order terms).
 *  Round each coefficient and exponent to two significant digits.
 **************************************************************************** */


Running time (in seconds) to find and remove one horizontal seam and one
vertical seam, as a function of both W and H:


    ~ 5.598*10^-8 * W^1.549 * H^0.754
       _______________________________________


Determining the correct formula was actually pretty hard because the ratios
weren't super consistent for either width or height. This is due to the fact
that my algorithm doesn't create a minimum spanning tree for the entire
picture, as that is actually very inefficient. Using Djikstra's algorithm,
once a path reaches the other side of the image, it is guaruanteed to be the
shortest path and so my algorithm stops calculating after that. However, how
fast it finds that path is very dependent on the contents of the image rather
than just the height and width of the image. If there is a path that has
significantly lower energy than any other path, the algorithm will be extremely
quick, but if there are multiple paths which all have very low energies, my
algorithm will have to expend more time calculating those paths. This algorithm
really shouldn't be modeled by a function of the form a * W^b * H^c because
it uses Dijkstra's algorithm, which has time complexity ElogV, which in this
case would be about 3 * W * H * log(W * H). However, to calculate a formula
for the type that they gave us, I averaged out the last 2 log ratios for the
running time findings of both H and W, since these are more accurate for
determining running time than experiments where W and H are smaller. To find
the constant factor "a", I calculated what a would be if we ran
W = 2000, H = 8000, and W = 8000, H = 2000 and averaged the 2.


/* *****************************************************************************
 *  Known bugs / limitations.
 **************************************************************************** */

I'm gonna try to optimize this algorithm over fall break using bitsets, since
the pixelTo array is pretty inefficient. Instead of storing the integer of
the pixel that precedes a given pixel, I can use 2 bits to dictate whether
the path goes up, straight, or down (for a horizontal seam) or if it goes left,
down, or right (for a vertical seam). Also I want to experiment with not
creating a new picture every time. I had the idea that instead of having to
construct an entirely new W x H image every time I removed a seam, I could just
set the color values of the pixels in the seam to -1, which would only require
time proportional to W or H instead of W x H. Then when I returned the picture
I could just read the pixels that aren't essentially nullified. However, this
would require having instance variables that kept track of the number of
vertical seams removed and the number of horizontal seams removed. 2 integer
instance variables shouldn't violate the memory constraints so I might try that.


/* *****************************************************************************
 *  Describe any serious problems you encountered.
 **************************************************************************** */




/* *****************************************************************************
 *  List any other comments here. Feel free to provide any feedback
 *  on how much you learned from doing the assignment, and whether
 *  you enjoyed doing it.
 **************************************************************************** */

Really cool assignment, and I'm quite proud of being able to figure out how
to correctly implement the algorithm by myself, although I will say knowing to
get the RGB integer versions of the colors rather than the color objects
themselves is not an obvious step for students, and I think that you should
make a note of that in the checklist part of the assignment. It would save
students a ton of time as they wouldn't have to consult office hours for a
relatively simple fix.
