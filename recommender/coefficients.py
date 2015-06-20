#!/usr/bin/env python

import numpy as np

## Input: n X 12 (segment by timbre component)
## Output: order x 12
def get_poly_coefficients(timbre_cols, timestamps, order):
    assert timbre_cols.shape[0] == timestamps.shape[0]
    def fit_series(ser_arr):
        return np.polyfit(timestamps, ser_arr, order)
    return np.column_stack(tuple(map(fit_series, timbre_cols.transpose())))


if __name__=='__main__':
    test = np.array([[0, 0], [1, 1], [4, 2]])
    ts = np.array([0, 1, 2])
    print get_poly_coefficients(test, ts, 2)
