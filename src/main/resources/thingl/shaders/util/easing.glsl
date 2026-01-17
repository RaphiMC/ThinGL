#include "math.glsl"

// === Sine ===

float easeInSine(float x) {
    return 1.0 - cos((x * PI) / 2.0);
}

float easeOutSine(float x) {
    return sin((x * PI) / 2.0);
}

float easeInOutSine(float x) {
    return -(cos(PI * x) - 1.0) / 2.0;
}

// === Quad ===

float easeInQuad(float x) {
    return pow(x, 2.0);
}

float easeOutQuad(float x) {
    return 1.0 - pow(1.0 - x, 2.0);
}

float easeInOutQuad(float x) {
    if (x < 0.5) {
        return 2.0 * pow(x, 2.0);
    } else {
        return 1.0 - pow(-2.0 * x + 2.0, 2.0) / 2.0;
    }
}

// === Cubic ===

float easeInCubic(float x) {
    return pow(x, 3.0);
}

float easeOutCubic(float x) {
    return 1.0 - pow(1.0 - x, 3.0);
}

float easeInOutCubic(float x) {
    if (x < 0.5) {
        return 4.0 * pow(x, 3.0);
    } else {
        return 1.0 - pow(-2.0 * x + 2.0, 3.0) / 2.0;
    }
}

// === Quart ===

float easeInQuart(float x) {
    return pow(x, 4.0);
}

float easeOutQuart(float x) {
    return 1.0 - pow(1.0 - x, 4.0);
}

float easeInOutQuart(float x) {
    if (x < 0.5) {
        return 8.0 * pow(x, 4.0);
    } else {
        return 1.0 - pow(-2.0 * x + 2.0, 4.0) / 2.0;
    }
}

// === Quint ===

float easeInQuint(float x) {
    return pow(x, 5.0);
}

float easeOutQuint(float x) {
    return 1.0 - pow(1.0 - x, 5.0);
}

float easeInOutQuint(float x) {
    if (x < 0.5) {
        return 16.0 * pow(x, 5.0);
    } else {
        return 1.0 - pow(-2.0 * x + 2.0, 5.0) / 2.0;
    }
}

// === Expo ===

float easeInExpo(float x) {
    if (x == 0.0) {
        return 0.0;
    } else {
        return pow(2.0, 10.0 * x - 10.0);
    }
}

float easeOutExpo(float x) {
    if (x == 1.0) {
        return 1.0;
    } else {
        return 1.0 - pow(2.0, -10.0 * x);
    }
}

float easeInOutExpo(float x) {
    if (x == 0.0) {
        return 0.0;
    } else if (x == 1.0) {
        return 1.0;
    } else if (x < 0.5) {
        return pow(2.0, 20.0 * x - 10.0) / 2.0;
    } else {
        return (2.0 - pow(2.0, -20.0 * x + 10.0)) / 2.0;
    }
}

// === Circ ===

float easeInCirc(float x) {
    return 1.0 - sqrt(1.0 - pow(x, 2.0));
}

float easeOutCirc(float x) {
    return sqrt(1.0 - pow(x - 1.0, 2.0));
}

float easeInOutCirc(float x) {
    if (x < 0.5) {
        return (1.0 - sqrt(1.0 - pow(2.0 * x, 2.0))) / 2.0;
    } else {
        return (sqrt(1.0 - pow(-2.0 * x + 2.0, 2.0)) + 1.0) / 2.0;
    }
}

// === Back ===

float easeInBack(float x) {
    float c1 = 1.70158;
    float c3 = c1 + 1.0;
    return c3 * pow(x, 3.0) - c1 * pow(x, 2.0);
}

float easeOutBack(float x) {
    float c1 = 1.70158;
    float c3 = c1 + 1.0;
    return 1.0 + c3 * pow(x - 1.0, 3.0) + c1 * pow(x - 1.0, 2.0);
}

float easeInOutBack(float x) {
    float c1 = 1.70158;
    float c2 = c1 * 1.525;
    if (x < 0.5) {
        return (pow(2.0 * x, 2.0) * ((c2 + 1.0) * 2.0 * x - c2)) / 2.0;
    } else {
        return (pow(2.0 * x - 2.0, 2.0) * ((c2 + 1.0) * (x * 2.0 - 2.0) + c2) + 2.0) / 2.0;
    }
}

// === Elastic ===

float easeInElastic(float x) {
    if (x == 0.0) {
        return 0.0;
    } else if (x == 1.0) {
        return 1.0;
    } else {
        float c4 = (2.0 * PI) / 3.0;
        return -pow(2.0, 10.0 * x - 10.0) * sin((x * 10.0 - 10.75) * c4);
    }
}

float easeOutElastic(float x) {
    if (x == 0.0) {
        return 0.0;
    } else if (x == 1.0) {
        return 1.0;
    } else {
        float c4 = (2.0 * PI) / 3.0;
        return pow(2.0, -10.0 * x) * sin((x * 10.0 - 0.75) * c4) + 1.0;
    }
}

float easeInOutElastic(float x) {
    if (x == 0.0) {
        return 0.0;
    } else if (x == 1.0) {
        return 1.0;
    } else {
        float c5 = (2.0 * PI) / 4.5;
        if (x < 0.5) {
            return -(pow(2.0, 20.0 * x - 10.0) * sin((20.0 * x - 11.125) * c5)) / 2.0;
        } else {
            return (pow(2.0, -20.0 * x + 10.0) * sin((20.0 * x - 11.125) * c5)) / 2.0 + 1.0;
        }
    }
}

// === Bounce ===

float easeOutBounce(float x) {
    float n1 = 7.5625;
    float d1 = 2.75;
    if (x < 1.0 / d1) {
        return n1 * pow(x, 2.0);
    } else if (x < 2.0 / d1) {
        x -= 1.5 / d1;
        return n1 * pow(x, 2.0) + 0.75;
    } else if (x < 2.5 / d1) {
        x -= 2.25 / d1;
        return n1 * pow(x, 2.0) + 0.9375;
    } else {
        x -= 2.625 / d1;
        return n1 * pow(x, 2.0) + 0.984375;
    }
}

float easeInBounce(float x) {
    return 1.0 - easeOutBounce(1.0 - x);
}

float easeInOutBounce(float x) {
    if (x < 0.5) {
        return (1.0 - easeOutBounce(1.0 - 2.0 * x)) / 2.0;
    } else {
        return (1.0 + easeOutBounce(2.0 * x - 1.0)) / 2.0;
    }
}

// === Power ===

float easeInPower(float x, float p) {
    return pow(x, p);
}

float easeOutPower(float x, float p) {
    return 1.0 - pow(1.0 - x, p);
}

float easeInOutPower(float x, float p) {
    if (x < 0.5) {
        return 0.5 * pow(2.0 * x, p);
    } else {
        return 1.0 - pow(-2.0 * x + 2.0, p) / 2.0;
    }
}
