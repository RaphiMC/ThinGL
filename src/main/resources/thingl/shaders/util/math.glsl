#define PI 3.14159265359
#define TWO_PI 6.28318530718
#define HALF_PI 1.57079632679

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float gaussian(float x, float sigma) {
    return exp(-pow(x, 2.0) / (2.0 * pow(sigma, 2.0)));
}
