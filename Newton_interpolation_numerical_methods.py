import numpy as np
import sympy as sp

# Define the datasets
x1 = np.array([2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 6.0])
y1 = np.array([4.32, 4.83, 5.27, 5.47, 6.26, 6.79, 7.23])

x2 = np.array([3, 4, 5, 6, 7, 8, 9])
y2 = np.array([13, 21, 31, 43, 57, 73, 91])

def calculate_forward_differences(y, stop_order):
    diffs = [y]
    while len(diffs[-1]) > 1 and len(diffs) <= stop_order:
        diffs.append(np.diff(diffs[-1]))
    return diffs

def has_consistent_alternating_signs(diffs):
    return all(np.sign(diffs[i]) * np.sign(diffs[i + 1]) < 0 for i in range(len(diffs) - 1))

def find_stop_order(y):
    forward_diffs = calculate_forward_differences(y, len(y))
    for i, diff in enumerate(forward_diffs):
        if np.all(diff == 0) or has_consistent_alternating_signs(diff):
            return i
    return len(forward_diffs) - 1

def newton_divided_difference(x, y, stop_order):
    n = stop_order + 1
    divided_diff = np.zeros((n, n))
    divided_diff[:, 0] = y[:n]
    for j in range(1, n):
        for i in range(n - j):
            divided_diff[i, j] = (divided_diff[i + 1, j - 1] - divided_diff[i, j - 1]) / (x[i + j] - x[i])
    return divided_diff

def newton_polynomial(x, divided_diff, stop_order):
    coefficients = divided_diff[0, :stop_order + 1]
    def polynomial(value):
        result, term = coefficients[0], 1.0
        for i in range(1, stop_order + 1):
            term *= (value - x[i - 1])
            result += coefficients[i] * term
        return result
    return polynomial

def get_polynomial_equation(coefficients, x, stop_order):
    x_sym = sp.symbols('x')
    equation, term = coefficients[0], 1
    for i in range(1, stop_order + 1):
        term *= (x_sym - x[i - 1])
        equation += coefficients[i] * term
    return sp.simplify(equation)

def process_dataset(x, y):
    stop_order = find_stop_order(y)
    diffs = calculate_forward_differences(y, stop_order)
    error_index = np.argmax(np.abs(diffs[-1]))
    error_range = x[error_index:error_index + stop_order + 1]
    divided_diff_table = newton_divided_difference(x, y, stop_order)
    polynomial = newton_polynomial(x, divided_diff_table, stop_order)
    interpolated_values = [polynomial(val) for val in error_range]
    differences = np.abs(interpolated_values - y[error_index:error_index + stop_order + 1])
    value_with_error = error_range[np.argmax(differences)]
    equation = get_polynomial_equation(divided_diff_table[0, :stop_order + 1], x, stop_order)
    return {
        'diffs': diffs,
        'stop_order': stop_order,
        'error_range': error_range,
        'divided_diff_table': divided_diff_table,
        'interpolated_values': interpolated_values,
        'differences': differences,
        'value_with_error': value_with_error,
        'equation': equation
    }

def print_results(title, results):
    print(f"{title}:")
    for i, diff in enumerate(results['diffs']):
        print(f"Order {i} differences: {diff}")
    print(f"Stop order: {results['stop_order']}")
    print(f"The range of x values that correspond to the highest value in the {results['stop_order']}th forward difference is {results['error_range']}.")
    print("Divided Difference Table:")
    print(results['divided_diff_table'])
    print(f"Interpolated values at x = {results['error_range']}: {results['interpolated_values']}")
    print(f"Differences between original and interpolated values: {results['differences']}")
    print(f"The value with the largest difference is {results['value_with_error']}.")
    print(f"Interpolation Polynomial Equation: P(x) = {results['equation']}\n")

results1 = process_dataset(x1, y1)
results2 = process_dataset(x2, y2)

print_results("Results for Dataset 1", results1)
print("="*50)
print_results("Results for Dataset 2", results2)
