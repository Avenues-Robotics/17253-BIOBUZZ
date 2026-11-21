---
title: Java ideas Peregrine uses
parent: Learn Peregrine
nav_order: 1
---

# Java ideas Peregrine uses
{: .no_toc }

Peregrine is built out of a handful of object-oriented ideas. You don't need to master them in the
abstract. This page explains each one using a piece of Peregrine you'll actually use, so that the
code on later pages makes sense.
{: .fs-5 .fw-300 }

1. TOC
{:toc}

## Classes and objects

A **class** is a blueprint. An **object** is one thing built from that blueprint.

`WaitTask` is a class. It describes how *any* wait works. When you write

```java
new WaitTask(this, 500)
```

you build one **object** from that blueprint: a particular wait of 500 milliseconds. You can build as
many as you like, and each one is separate. Two `WaitTask` objects each keep their own timer, the same
way two houses built from the same plan each have their own front door.

The keyword `new` is what builds an object.

## Fields

A **field** is a variable that belongs to an object. Here is the start of `WaitTask`:

```java
public class WaitTask extends Task {

    long time;                // how long to wait, in milliseconds
    ElapsedTime elapsedTime;  // started on the first run()
```

Every `WaitTask` object has its own `time` and its own `elapsedTime`. That's how two waits can be
running at once without getting mixed up.

## Constructors and `this`

A **constructor** is a special method that runs once, when the object is built with `new`. Its name
is the same as the class, and it has no return type. It usually copies its inputs into fields:

```java
public WaitTask(PeregrineOpMode opMode, long timeMs) {
    this.opMode = opMode;
    time = timeMs;
}
```

`this` means "the object being built right now". `this.opMode = opMode;` reads as "set **this
object's** `opMode` field to the `opMode` that was passed in". You need `this.` when a field and a
parameter have the same name. Otherwise Java can't tell them apart.

## Methods and return values

A **method** is a named block of code that belongs to a class. It can hand back a value with
`return`. Every task has a method like this:

```java
public boolean run() {
    ...
    return true;   // "I'm finished"
}
```

`boolean` before the name means the method returns `true` or `false`. `void` means it returns
nothing.

## Inheritance: `extends`

A class can be built **on top of** another class with `extends`. The new class (the **subclass**)
gets everything the old class (the **superclass**) has, and adds its own parts.

```java
public class MyTeleop extends PeregrineOpMode { ... }
```

`PeregrineOpMode` already contains hundreds of lines of setup: it maps the hardware, starts odometry,
and runs the main loop. By writing `extends PeregrineOpMode`, `MyTeleop` gets all of that for free and
only has to fill in the parts that are specific to it.

Likewise, every task `extends Task`, which is how it gets the `opMode` field without declaring it.

## Abstract classes: fill in the blanks

Some classes are deliberately incomplete. They're marked **`abstract`**, and they have
**abstract methods**: methods with a name but no body. You can't build an object from an abstract
class directly. Instead, you extend it and fill in every blank.

`Task` is abstract, with three blanks:

```java
public abstract class Task {
    protected PeregrineOpMode opMode;

    public abstract boolean run();   // no body: every task must write its own
    public abstract void end();
    public abstract Task reset();
}
```

This is a **contract**. It says "anything that wants to be a task must be able to `run()`, `end()`
and `reset()`". Because every task keeps that promise, Peregrine can run any task without knowing
what it does.

If you forget a blank, Android Studio shows a red error along the lines of *"Class must either be
declared abstract or implement abstract method"*. Click on the class name and press **Alt+Enter**
(**Option+Enter** on a Mac), then choose **Implement methods** to have it write empty versions for
you.

## `@Override`

When a subclass fills in (or replaces) a method from its superclass, you put `@Override` above it:

```java
@Override
public boolean run() {
    ...
}
```

It doesn't change what the code does. It asks the compiler to check that there really is a method
with that name to override, so a typo like `public boolean runn()` becomes an error instead of a
silent bug.

## Polymorphism: one type, many kinds

This is the idea that makes Peregrine work. A variable of type `Task` can hold **any** kind of task:

```java
Task a = new WaitTask(this, 500);
Task b = new Drive(this, "park");
Task c = new SetClaw(this, 1);      // a task you'll write yourself
```

When Peregrine calls `a.run()`, the `WaitTask` version runs. When it calls `b.run()`, the `Drive`
version runs. Peregrine doesn't need to know or care which is which. It just knows they're all tasks.

That's why `SeriesTask` can hold a mix of anything:

```java
new SeriesTask(new SetClaw(this, 0), new WaitTask(this, 500), new Drive(this, "park"))
```

## Access: `public`, `private`, `protected`

These words control who can see a field or method:

| Word | Who can use it |
| --- | --- |
| `public` | Any code anywhere. |
| `private` | Only code inside the same class. |
| `protected` | The same class, its subclasses, and classes in the same package. This is why your task can use `opMode`, which `Task` declares as `protected`. |
| *(nothing)* | Classes in the same package. |

## `static`: belongs to the class, not an object

A **static** field or method belongs to the class itself, so there's only one copy, shared by
everything. You use it through the class name, without `new`:

```java
RobotParams.chassis                        // a static field
Kinematics.powerMotors(0.5, 0, 0, opMode); // a static method
```

`RobotParams` is all static fields because there's only one robot.

## Annotations: labels in `@`

An **annotation** is a label that starts with `@`. It doesn't run anything itself; other code reads
it.

| Annotation | Who reads it | What it means |
| --- | --- | --- |
| `@TeleOp`, `@Autonomous` | The FTC app | Show this opMode on the Driver Station. |
| `@Override` | The compiler | Check that this really overrides a method. |
| `@Ends` | Peregrine | This task always finishes on its own. |
| `@Config` | FTC Dashboard | Let me edit this class's static fields live. |

## Lambdas: passing code as a value

Sometimes a task needs a small piece of *your* code: "which button?", "what should happen?". Instead
of writing a whole class for it, you write a **lambda**, a tiny unnamed method:

```java
() -> gamepad1.a
```

Read it as "a function that takes nothing, `()`, and gives back `gamepad1.a`". Some more:

```java
new WaitUntilTask(() -> hardware.touch.isPressed())       // wait until the sensor is pressed
new InstantTask(() -> hardware.claw.setPosition(1))       // run this line once
new TeleopTask(this, new SetClaw(this, 0), () -> gamepad1.a, true)
```

{: .warning }
The code in a lambda does **not** run when you write it. It runs **later**, every time the task
calls it. `() -> gamepad1.a` is checked fresh every loop, which is exactly what you want for a
button.

Under the hood, a lambda fills in an **interface**: a type with a single blank method, like
`BooleanSupplier` ("give me a `true`/`false`") or `Runnable` ("do something"). You rarely need to
name these. Just write the lambda where the task asks for it.

## `Task...`: any number of arguments

A parameter written with three dots accepts any number of values:

```java
public SeriesTask(Task... tasks)
```

So `new SeriesTask(a, b)` and `new SeriesTask(a, b, c, d, e)` both work.

## `null`

A field that holds an object starts out as `null`, meaning "nothing yet". Peregrine tasks often use
this to do something only on the first run:

```java
if (timer == null) timer = new ElapsedTime();   // only true the very first time
```

Calling a method on something that is `null` crashes with a `NullPointerException`, so when you see
one, look for a field you forgot to set.

## Check yourself

1. What's the difference between `WaitTask` and `new WaitTask(this, 500)`?
2. Why can a `SeriesTask` hold both a `Drive` and a `WaitTask`?
3. When does the code in `() -> gamepad1.a` run?
4. Your class `MyTask extends Task` has a red error under its name. What's the most likely cause?

<details markdown="block">
<summary>Answers</summary>

1. `WaitTask` is the class (the blueprint). `new WaitTask(this, 500)` builds one object from it.
2. Both are tasks: they both extend `Task`, so they can be used anywhere a `Task` is expected.
3. Every time the task that holds it calls it, which is usually once per loop. Not when you write it.
4. It doesn't implement all three abstract methods (`run`, `end`, `reset`). Use Alt+Enter →
   **Implement methods**.

</details>

Next: [How Peregrine runs](how-it-runs.md).
