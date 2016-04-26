package org.luzai;

/**
 * Created by luzai on 4/26/2016.
 */

class Particle {
    public float mPosX;
    public float mPosY;
    public float mAccelX;
    public float mAccelY;
    public float mLastPosX;
    public float mLastPosY;
    public float mOneMinusFriction;

    Particle(float sFriction) {
        // make each particle a bit different by randomizing its
        // coefficient of friction
        final float r = ((float) Math.random() - 0.5f) * 0.2f;
        mOneMinusFriction = 1.0f - sFriction + r;
    }

    public void computePhysics(float sx, float sy, float dT, float dTC) {
        // Force of gravity applied to our virtual object
        final float m = 1000.0f; // mass of our virtual object
        final float gx = -sx * m;
        final float gy = -sy * m;

                /*
                 * F = mA <=> A = F / m We could simplify the code by
                 * completely eliminating "m" (the mass) from all the equations,
                 * but it would hide the concepts from this sample code.
                 */
        final float invm = 1.0f / m;
        final float ax = gx * invm;
        final float ay = gy * invm;

                /*
                 * Time-corrected Verlet integration The position Verlet
                 * integrator is defined as x(t+dt) = x(t) + x(t) - x(t-dt) +
                 * a(t).t^2 However, the above equation doesn't handle variable
                 * dt very well, a time-corrected version is needed: x(t+dt) =
                 * x(t) + (x(t) - x(t-dt)) * (dt/dt_prev) + a(t).t^2 We also add
                 * a simple friction term (f) to the equation: x(t+dt) = x(t) +
                 * (1-f) * (x(t) - x(t-dt)) * (dt/dt_prev) + a(t)t^2
                 */
        final float dTdT = dT * dT;
        final float x = mPosX + mOneMinusFriction * dTC * (mPosX - mLastPosX) + mAccelX
                * dTdT;
        final float y = mPosY + mOneMinusFriction * dTC * (mPosY - mLastPosY) + mAccelY
                * dTdT;
        mLastPosX = mPosX;
        mLastPosY = mPosY;
        mPosX = x;
        mPosY = y;
        mAccelX = ax;
        mAccelY = ay;
    }

    /*
     * Resolving constraints and collisions with the Verlet integrator
     * can be very simple, we simply need to move a colliding or
     * constrained particle in such way that the constraint is
     * satisfied.
     */
    public void resolveCollisionWithBounds(float mHorizontalBound, float mVerticalBound) {
        final float xmax = mHorizontalBound;
        final float ymax = mVerticalBound;
        final float x = mPosX;
        final float y = mPosY;
        if (x > xmax) {
            mPosX = xmax;
        } else if (x < -xmax) {
            mPosX = -xmax;
        }
        if (y > ymax) {
            mPosY = ymax;
        } else if (y < -ymax) {
            mPosY = -ymax;
        }
    }
}

/*
 * A particle system is just a collection of particles
 */

public class ParticleSystem {
    // diameter of the balls in meters
    private static final float sBallDiameter = 0.004f;
    private static final float sBallDiameter2 = sBallDiameter * sBallDiameter;
    private float mHorizontalBound, mVerticalBound;
    // friction of the virtual table and air
    private static final float sFriction = 0.1f;
    static final int NUM_PARTICLES = 3;
    private Particle mBalls[] = new Particle[NUM_PARTICLES];

    ParticleSystem(float mHorizontalBound,float mVerticalBound) {

        /*
         * Initially our particles have no speed or acceleration
         */
        this.mHorizontalBound=mHorizontalBound;
        this.mVerticalBound=mVerticalBound;
        for (int i = 0; i < mBalls.length; i++) {
            mBalls[i] = new Particle(sFriction);
        }
    }

    /*
     * Update the position of each particle in the system using the
     * Verlet integrator.
     */

    private long mLastT;
    private float mLastDeltaT;

    private void updatePositions(float sx, float sy, long timestamp) {
        final long t = timestamp;
        if (mLastT != 0) {
            final float dT = (float) (t - mLastT) * (1.0f / 1000000000.0f);
            if (mLastDeltaT != 0) {
                final float dTC = dT / mLastDeltaT;
                final int count = mBalls.length;
                for (int i = 0; i < count; i++) {
                    Particle ball = mBalls[i];
                    ball.computePhysics(sx, sy, dT, dTC);
                }
            }
            mLastDeltaT = dT;
        }
        mLastT = t;
    }

    /*
     * Performs one iteration of the simulation. First updating the
     * position of all the particles and resolving the constraints and
     * collisions.
     */
    public void update(float sx, float sy, long now) {
        // update the system's positions
        updatePositions(sx, sy, now);

        // We do no more than a limited number of iterations
        final int NUM_MAX_ITERATIONS = 10;

                /*
                 * Resolve collisions, each particle is tested against every
                 * other particle for collision. If a collision is detected the
                 * particle is moved away using a virtual spring of infinite
                 * stiffness.
                 */
        boolean more = true;
        final int count = mBalls.length;
        for (int k = 0; k < NUM_MAX_ITERATIONS && more; k++) {
            more = false;
            for (int i = 0; i < count; i++) {
                Particle curr = mBalls[i];
                for (int j = i + 1; j < count; j++) {
                    Particle ball = mBalls[j];
                    float dx = ball.mPosX - curr.mPosX;
                    float dy = ball.mPosY - curr.mPosY;
                    float dd = dx * dx + dy * dy;
                    // Check for collisions
                    if (dd <= sBallDiameter2) {
                                /*
                                 * add a little bit of entropy, after nothing is
                                 * perfect in the universe.
                                 */
                        dx += ((float) Math.random() - 0.5f) * 0.0001f;
                        dy += ((float) Math.random() - 0.5f) * 0.0001f;
                        dd = dx * dx + dy * dy;
                        // simulate the spring
                        final float d = (float) Math.sqrt(dd);
                        final float c = (0.5f * (sBallDiameter - d)) / d;
                        curr.mPosX -= dx * c;
                        curr.mPosY -= dy * c;
                        ball.mPosX += dx * c;
                        ball.mPosY += dy * c;
                        more = true;
                    }
                }
                        /*
                         * Finally make sure the particle doesn't intersects
                         * with the walls.
                         */
                curr.resolveCollisionWithBounds(mHorizontalBound,mVerticalBound);
            }
        }
    }

    public int getParticleCount() {
        return mBalls.length;
    }

    public float getPosX(int i) {
        return mBalls[i].mPosX;
    }

    public float getPosY(int i) {
        return mBalls[i].mPosY;
    }
}
