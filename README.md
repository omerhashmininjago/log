# log

This library contains an annotation which can be used to track the execution time of a method

This annotation can be placed on top of classes and methods

If placed on top of a class, then every method which is being referenced from outside the class, will be considered as part of profiling

The developer has the option to get the time taken to be logged at log level of his own choice, for this he would need to set the value of LogLevel with in the annotation.

The default value is INFO.
