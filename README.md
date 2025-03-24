Task 1: Gamma Correction:

•	Implement gamma correction using the standard gamma equation:
•	Corrected Value=(originalvalue/255)^ 1/γ 
•	For full marks, implement this using a Look-Up Table (LUT) approach for efficiency.
•	Watch the provided "Coding Gamma Correction" video for guidance.
•	Using the base code is encouraged. Your code may resemble the video example.

Task 2: Image Resizing:

•	Implement image resizing using two different interpolation methods:
•	Nearest Neighbour Interpolation
•	Bilinear Interpolation
•	Nearest Neighbour interpolation is demonstrated in the same video as Task 1.
•	Together with gamma correction, these two tasks contribute up to 45% of the overall marks.

Task 3: Laplacian Cross Correlation:

•	Implement cross-correlation using a filter (Laplacian matrix).
•	For each pixel, compute a weighted sum with its neighbourhood.
•	Apply normalisation to convert the result to a displayable format.
•	Use the following 5x5 Laplacian filter:

-4 -1  0 -1 -4
-1  2  3  2 -1
 0  3  4  3  0
-1  2  3  2 -1
-4 -1  0 -1 -4
![Uploading image.png…]()
