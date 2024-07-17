import numpy as np

# Given data
x = np.array([2, 4, 5, 8, 10])
y = np.array([23, 93, 259, 596, 1071])

# Calculate the forward differences
def calculate_forward_differences(y, stop_order):
    diffs = [y]
    while len(diffs[-1]) > 1 and len(diffs) <= stop_order:
        diffs.append(np.diff(diffs[-1]))
    return diffs

# Check for consistent alternating signs
def has_consistent_alternating_signs(diffs):
    signs = np.sign(diffs)
    return all(signs[i] * signs[i + 1] < 0 for i in range(len(signs) - 1))

# Determine the order at which to stop based on consistent alternating signs
stop_order = next((i for i, diff in enumerate(calculate_forward_differences(y, len(y))) if has_consistent_alternating_signs(diff)), None)

# Calculate differences up to the stop order
diffs = calculate_forward_differences(y, stop_order)

# Find the index of the value with the highest absolute magnitude in the last forward difference
error_index = np.argmax(np.abs(diffs[-1]))

# Identify the range of x values that correspond to the error
error_range = x[error_index:error_index+stop_order+1]

# Print the differences, stop order, and error range
for i, diff in enumerate(diffs):
    print(f"Order {i} differences: {diff}")
print(f"Stop order: {stop_order}")
print(f"The range of x values that correspond to the highest value in the {stop_order}th forward difference is {error_range}.")

# Compute the divided difference table
def newton_divided_difference(x, y):
    n = len(x)
    divided_diff = np.zeros((n, n))
    divided_diff[:, 0] = y

    for j in range(1, n):
        for i in range(n - j):
            divided_diff[i, j] = (divided_diff[i + 1, j - 1] - divided_diff[i, j - 1]) / (x[i + j] - x[i])
    
    return np.trunc(divided_diff)

divided_diff_table = newton_divided_difference(x, y)

# Print the divided difference table
print("Divided Difference Table:")
print(divided_diff_table)

# Generate the interpolation polynomial
def newton_polynomial(x, divided_diff):
    n = len(x)
    coefficients = divided_diff[0, :]
    
    def polynomial(value):
        result = coefficients[0]
        term = 1.0
        for i in range(1, n):
            term *= (value - x[i - 1])
            result += coefficients[i] * term
        return result
    
    return polynomial

polynomial = newton_polynomial(x, divided_diff_table)

# Example: Evaluate the polynomial at given values in the error range
values_to_interpolate = error_range
interpolated_values = [polynomial(val) for val in values_to_interpolate]

# Calculate the differences and find the value with the largest difference
differences = np.abs(interpolated_values - y[error_index:error_index+stop_order+1])
max_diff_index = np.argmax(differences)
value_with_error = error_range[max_diff_index]

# Print the results
print(f"\nInterpolated values at x = {values_to_interpolate}: {interpolated_values}")
print(f"Differences between original and interpolated values: {differences}")
print(f"The value with the largest difference is {value_with_error}.")

# Get the equation of the interpolation
def get_polynomial_equation(coefficients, x):
    n = len(coefficients)
    equation = f"{coefficients[0]}"
    for i in range(1, n):
        term = " * ".join([f"(x - {x[j]})" for j in range(i)])
        equation += f" + {coefficients[i]} * {term}"
    return equation

coefficients = divided_diff_table[0, :]
equation = get_polynomial_equation(coefficients, x)
print(f"\nInterpolation Polynomial Equation: P(x) = {equation}")