package net.woggioni.worth.serialization.json;

class Chronometer {

    public enum TimeUnit {
        NANOSECOND(1e-9), MICROSECOND(1e-6), MILLISECOND(1e-3), SECOND(1);

        private double factor;

        TimeUnit(double factor) {
            this.factor = factor;
        }
    }

    private long start = System.nanoTime();

    public void start() {
        start = System.nanoTime();
    }

    public void reset() {
        start();
    }

    public double stop(TimeUnit unit) {
        return (System.nanoTime() - start) / (1e9 * unit.factor);
    }

    public double stop() {
        return stop(TimeUnit.MILLISECOND);
    }

}

