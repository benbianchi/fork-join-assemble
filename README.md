# Intro

Hi, I decided to test Akka out, and see if I could build fork and join using the `ask` pattern. I then wanted to see if I could change the fork and join paradigm, to 
instead of awaiting all work to be done in the forker thread, to instruct all subwork thread with a way to understand if the work is done, and a ref
to send the message to, when it is finished.

# Results

I found that using the Ask method was approximately 19  seconds, while this fork-join-assemble with no awaits allwed us to send at 10 seconds.
