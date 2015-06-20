#!/usr/bin/env python

import numpy as np

X_STEP_SIZE= .01

## Input: n X 12 (segment by timbre component)
## Output: order x 12
def get_poly_coefficients(timbre_cols, order):
    def fit_series_with_order(ser_arr):
        return fit_series(ser_arr, order)

    return np.column_stack(tuple(map(fit_series_with_order, timbre_cols.transpose())))

def fit_series(ser_arr, order):
    num_seq = range(len(ser_arr))
    x_ax = np.array(map(lambda x: x * X_STEP_SIZE, num_seq))
    y_ax = np.array(ser_arr)
    return np.polyfit(x_ax, y_ax, order)


if __name__=='__main__':
    test = np.array([[0, 0], [.0001, .01], [.0004, .02]])
    print get_poly_coefficients(test, 2)
