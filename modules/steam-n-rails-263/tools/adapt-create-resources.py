"""Adapt processed Create Fly resources to the pinned Create Adapted codecs.
Run only against generated build output. Source JSON remains upstream-compatible.
"""
from pathlib import Path
import argparse, json

def convert(node, counts):
    if isinstance(node, list):
        for child in node: convert(child, counts)
    elif isinstance(node, dict):
        if node.get("neoforge:type") == "neoforge:components":
            node["neoforge:ingredient_type"] = node.pop("neoforge:type")
        recipe_type = node.get("type")
        if recipe_type == "create:sequenced_assembly" and "result" in node:
            transition = node["transitional_item"]["id"]
            node["results"] = [node.pop("result")]
            def expand(value):
                if value == "$ingredient": return transition
                if value == "$result": return {"id": transition}
                if isinstance(value, list): return [expand(v) for v in value]
                if isinstance(value, dict): return {k: expand(v) for k,v in value.items()}
                return value
            node["sequence"] = expand(node["sequence"])
            counts["sequenced"] += 1
        if recipe_type in ("create:item_application", "create:deploying", "create:cutting", "create:pressing"):
            if "ingredient" in node:
                ingredients = [node.pop("ingredient")]
                if "target" in node: ingredients.insert(0, node.pop("target"))
                if "ingredients" in node: raise ValueError("Conflicting ingredient schemas")
                node["ingredients"] = ingredients
                counts["item_processing"] += 1
            if "results" in node:
                node["results"] = [{"id": v} if isinstance(v, str) else v for v in node["results"]]
        for child in list(node.values()): convert(child, counts)
        if "fluid_ingredients" in node or "fluid_results" in node:
            for source, target in (("fluid_ingredients", "ingredients"), ("fluid_results", "results")):
                output = node.setdefault(target, [])
                for fluid in node.pop(source, []):
                    amount = fluid["amount"]
                    if not isinstance(amount, int) or amount <= 0 or amount % 81:
                        raise ValueError(f"Non-integral droplet to mB conversion: {fluid}")
                    amount //= 81
                    if source == "fluid_ingredients":
                        if fluid.get("type") != "fluid_stack":
                            raise ValueError(f"Unrecognized Create Fly fluid ingredient: {fluid}")
                        ingredient = fluid["fluid"]
                        if fluid.get("components"):
                            ingredient = {"neoforge:ingredient_type": "neoforge:components", "fluids": ingredient,
                                          "components": fluid["components"], "strict": False}
                        output.append({"ingredient": ingredient, "amount": amount})
                    else:
                        output.append(dict(fluid, amount=amount))
                    counts[source] += 1
            counts["recipes"] += 1

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    root = args.output.resolve()
    if root.name != "main" or root.parent.name != "resources" or root.parent.parent.name != "build":
        raise ValueError("Only this project's generated build/resources/main may be converted")
    counts = {"recipes": 0, "fluid_ingredients": 0, "fluid_results": 0, "projectiles": 0, "sequenced": 0, "item_processing": 0, "track_nugget_tags": 0}
    for path in sorted((root / "data").glob("*/recipe/**/*.json")):
        before = path.read_text(encoding="utf-8")
        counts["track_nugget_tags"] += before.count('"#create:track_nuggets"')
        value = json.loads(before.replace('"#create:track_nuggets"', '"#railways:internal/track_nuggets"'))
        convert(value, counts)
        after = json.dumps(value, ensure_ascii=False, indent=2) + "\n"
        if json.loads(before) != value: path.write_text(after, encoding="utf-8", newline="\n")
    # NeoForge's custom world registry is create:potato_projectile/type.
    # Its files use the registry namespace segment, unlike Create Fly's loader.
    for path in sorted((root / "data").glob("*/potato_projectile/**/*.json")):
        relative = path.relative_to(root / "data")
        target = root / "data" / relative.parts[0] / "create" / Path(*relative.parts[1:])
        target.resolve().relative_to(root)
        target.parent.mkdir(parents=True, exist_ok=True)
        content = path.read_bytes()
        if target.exists() and target.read_bytes() != content: raise ValueError(f"Conflicting projectile {target}")
        target.write_bytes(content)
        path.unlink()
        counts["projectiles"] += 1
    print("Create Adapted resource conversion: " + json.dumps(counts, sort_keys=True))

if __name__ == "__main__": main()
