import pandas as pd

data = pd.read_csv("listings.csv")
data["available"] = False

print(data.head())

data.to_json("listings.json",orient="records")
