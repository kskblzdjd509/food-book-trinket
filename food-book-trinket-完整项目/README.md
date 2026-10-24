# Food Book Trinket (食物之书)

一个 Minecraft 1.20.1 Forge 的 Curios 饰品模组。

## 简介

食物之书是一件 **Curios 配饰（charm 栏位）**。当玩家佩戴它并食用一种**新食物**时，会永久获得 **+0.2 攻击力**；重复食用同一种食物不会再次加成。

## 环境要求

- Minecraft **1.20.1**
- Forge **47.4.28**（对应版本范围 `[47.4,48)`）
- **Curios API** 5.4.7（`curios-forge-1.20.1-5.4.7` 或更高 5.x）

## 构建

```bash
# 需要 JDK 17 与 Gradle（项目使用 wrapper 或系统 Gradle）
gradlew build
# 产物位于 build/libs/foodbooktrinket-1.0.0.jar
```

## 玩法说明

1. 通过 `/curios`（Curios 自带）或积分/工作台获得的 **食物之书** 放入角色 **配饰（charm）** 栏位。
2. 佩戴状态下食用任意一种**从未吃过**的食物，攻击力永久 +0.2。
3. 已经吃过的食物再次食用不会叠加。

## 实现要点

- **食物历史**：通过 Forge Capability (`IFoodHistory`) 记录在玩家持久化 NBT 中，跨存档/跨玩家独立。
- **攻击力加成**：通过给玩家 `ATTACK_DAMAGE` 属性附加一个固定 UUID 的 `ADDITION` 修饰符，数值 = 已食用新食物种类数 × 0.2。
- **持久化**：玩家登录/复活/跨维度 clone 时根据历史记录重新应用加成。
- **栏位限制**：`FoodBookTrinketItem#canEquip` 限定只能放入 `charm`（配饰）栏位。

## 代码结构

```
src/main/java/com/example/foodbooktrinket/
├── FoodBookTrinket.java           主类（@Mod）
├── FoodBookConstants.java         常量
├── item/FoodBookTrinketItem.java  饰品物品类（Curios 集成）
└── tracking/
    ├── IFoodHistory.java          食物历史接口 + Stub
    ├── FoodHistoryCapability.java Capability 实现
    └── FoodHistoryEvents.java     进食/登录事件处理
```

## 许可

MIT
