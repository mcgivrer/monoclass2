# Contributing to MonoClass2

## Introduction

This little game framework is designed and developed into a Knowledge enhancement and a discovering purpose.
Nothing about creating and publishing the next killer game on IPhone/Android/Console/PC.

So be cool and show mercy to the project owner/author who I am; I am not a professional game developer, but an old and experienced java developer and an industrial software architect who try to disover new computer areas, with fun and pleasure, to bring some new vision of processing into some "more" serious domains ;)

## pre-requisite

- Contributing to this small project is authorized as soon as you are already a Java developer ;)
- Bringing enhancement to any part of the code is almost welcome.
- Bringing new knowledge to the proposed stack is very welcome :+1:

## Some basic rules

### Code management

1. When contributing, follow the famous **git-flow** process to create and manage your git branches/features. Please see the [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/ "open the post") official page for installation and usage details.

2. Respect the Git principe of "Pull Request" to be able to merge your work to the `develop` branch. And you will only be able to merge as soon code quality and CI build will be ok.

### About Doc

3. Before coding, start by a standard "Issue" and choose the right type: `Bug report` or `Feature Request`. Add as details as possible describing tightly your idea. Don't hesitate to write a wiki page with some diagram if needed, to explain to the project owner as if he was a _3 years old child_.

### Code Room

4. Then, provides code ! but respect the java standard code writing rules, mainly the [CheckStyles](http://checkstyle.sourceforge.net/ "gt and visit the official site") default configuration.
   - Any core eligible feature (see bellow) will be pushed to the `core` package.
   - other feature(s) or tools, will go to the `contributes` package.
   - sample and examples will fall into `demo`.

> Bring some example(s) of your feature(s)/tool(s)/system(s) into the `demo` package. this is mandatory to any new added ${projectname} capacity to be illustrated on its usage.

### Testing !

5. Coding is not the only thing. Each time it is possible, build unit test to cover the contributed code. Use [Junit 5](https://junit.org/junit5/ "open the junit portal"), [Cucumber](https://docs.cucumber.io/ "go to the cucumber's community") or any unit testing library that brings clarity to tests and code validation. A JaCoCo process evaluates code coverage (as soon as this one will be activated).

### Law and Order

6. Please be careful with new libraries you want to embed in the project. The Project License is MIT and be sure to not break the rules by adding other kind of library license in the scope of this open project.

7. If you need graphism or any pixels to the demo/sample, be sure you get the pixel artist authorization before commiting things, and add the credits to the dedicated part of the README.md file.

## Enjoy !

Anyway you contribute on the project, please take pleasure and have fun developing cool and beautiful( wjhen it's possible) thing.

McGivrer.
