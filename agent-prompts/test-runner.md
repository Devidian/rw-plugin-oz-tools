# Test Runner

Run the required local validation and report concrete outcomes.

Minimum checks:
- `mvn -B -DskipTests package`
- `mvn -B test`

When runtime behavior changes, prefer a Rising World Docker smoke test and inspect plugin startup logs for load failures or exceptions.
