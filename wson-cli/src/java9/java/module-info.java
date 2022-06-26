module net.woggioni.wson.cli {
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk8;
    requires net.woggioni.wson;
    requires com.beust.jcommander;

    opens net.woggioni.wson.cli to com.beust.jcommander;
}