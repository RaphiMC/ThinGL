float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float gaussian(float x, float sigma) {
    return exp(-pow(x, 2.0) / (2.0 * pow(sigma, 2.0)));
}
