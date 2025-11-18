# Contributing to Another Git Worktree

First off, thank you for considering contributing to Another Git Worktree! It's people like you that make it a great tool.

## Code of Conduct

This project and everyone participating in it is governed by respect and professionalism. By participating, you are expected to uphold this standard.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* **Use a clear and descriptive title**
* **Describe the exact steps to reproduce the problem**
* **Provide specific examples to demonstrate the steps**
* **Describe the behavior you observed and what behavior you expected to see**
* **Include screenshots if possible**
* **Include your environment details** (OS, IDEA version, plugin version, Git version)
* **Attach relevant logs** from Help → Show Log in Explorer

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* **Use a clear and descriptive title**
* **Provide a step-by-step description of the suggested enhancement**
* **Provide specific examples to demonstrate the steps**
* **Describe the current behavior and explain the expected behavior**
* **Explain why this enhancement would be useful**

### Pull Requests

* Fill in the pull request template
* Follow the Kotlin coding style used throughout the project
* Include screenshots in your pull request whenever possible
* End all files with a newline
* Avoid platform-dependent code

## Development Setup

### Prerequisites

* IntelliJ IDEA 2025.1+
* JDK 17+
* Git 2.15+

### Setting Up Development Environment

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/your-username/another_gitworktree.git
   cd another_gitworktree
   ```

3. Open the project in IntelliJ IDEA

4. Build the project:
   ```bash
   ./gradlew build
   ```

5. Run the plugin in development mode:
   ```bash
   ./gradlew runIde
   ```

### Project Structure

```
another_gitworktree/
├── src/main/kotlin/           # Kotlin source files
│   └── io/github/nwen/another_gitworktree/
│       ├── WorktreePanel.kt          # Main UI panel
│       ├── WorktreeTabInstaller.kt   # Tab installer
│       └── OpenWorktreeTabAction.kt  # Reopen action
├── src/main/resources/        # Resources
│   └── META-INF/
│       └── plugin.xml         # Plugin configuration
├── build.gradle.kts           # Build configuration
└── README.md                  # Documentation
```

### Coding Guidelines

* Follow Kotlin coding conventions
* Write clear, self-documenting code
* Add comments for complex logic
* Keep functions small and focused
* Use meaningful variable and function names
* Handle errors gracefully with user-friendly messages

### Git Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Use conventional commits format:
  * `feat:` for new features
  * `fix:` for bug fixes
  * `docs:` for documentation changes
  * `style:` for formatting changes
  * `refactor:` for code refactoring
  * `test:` for adding tests
  * `chore:` for maintenance tasks

Examples:
```
feat: add support for remote branch worktrees
fix: resolve EDT slow operation warning
docs: update installation instructions
```

### Testing

Before submitting a pull request:

1. Build the project: `./gradlew build`
2. Test manually by running: `./gradlew runIde`
3. Test all affected features thoroughly
4. Check for any linter warnings or errors

### Documentation

* Update README.md if you change functionality
* Update inline code comments for complex logic
* Add JSDoc comments for public functions
* Keep both English and Chinese documentation in sync

## Questions?

Feel free to create an issue with your question, and we'll be happy to help!

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

