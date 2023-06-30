# Worknote

Welcome to the Worknote repository! 
This project is dedicated to providing a seamless solution 
for synchronizing worklogs with a private corporate Jira server. 
The system aims to simplify the process of recording work time 
into journals, providing a centralized location for managing worklogs 
and assisting users with prompts for recurring events and tasks.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)

## Introduction

At workplaces, it is often required to track and document worklogs in journals or similar systems. 
Worknote offers a convenient way to enter worklogs 
and effortlessly synchronize them with your private corporate Jira server. 
The system provides a unified interface where users can easily manage their work time, 
view weekends and reduced working days, 
and receive prompts for recurring events and tasks 
that require time allocation.

## Features

- **Synchronization**: Seamlessly synchronize worklogs with your private corporate Jira server.
- **Centralized Management**: All worklogs are stored and managed in a single location, providing easy access and organization.
- **Weekend and Reduced Working Day Display**: Clearly visualize weekends and reduced working days to effectively plan and allocate work time.
- **Remaining Time Tracking**: Easily view how much time is left to write off for a specific day, ensuring accurate time allocation.
- **Recurring Event and Task Prompts**: Receive prompts for recurring events and tasks that require time allocation, ensuring no worklogs are missed.

## Installation

To install Worknote, follow these steps:

1. Clone the repository: `git clone https://github.com/your-username/worklog-sync.git`
2. Navigate to the project directory: `cd worklog-sync`
3. Install dependencies: `npm install`

## Usage

To use Worknote, follow these steps:

1. Ensure you have completed the installation steps.
2. Launch the application: `npm start`
3. Access the application through your preferred web browser at `http://localhost:3000`.
4. Sign in to your private corporate Jira instance using your credentials.
5. Enter your worklogs, taking advantage of the system's features such as weekend and reduced working day display, remaining time tracking, and recurring event and task prompts.
6. Click the sync button to synchronize your worklogs with Jira.

## Configuration

Before using Worknote, you need to configure it by providing the necessary credentials and settings. Follow these steps to configure the system:

1. Open the `config.js` file located in the project root directory.
2. Update the `jiraUrl` field with the URL of your private corporate Jira instance.
3. Update the `username` and `password` fields with your Jira login credentials.
4. Save the changes.

## Contributing

Contributions are welcome! If you'd like to contribute to the Worklog Sync with Jira project, please follow these guidelines:

1. Fork the repository.
2. Create a new branch: `git checkout -b feature/my-feature`
3. Make your changes and commit them: `git commit -m 'Add new feature'`
4. Push the changes to your branch: `git push origin feature/my-feature`
5. Submit a pull request detailing your changes.

## License

The Worklog Sync with Jira project is licensed under the [MIT License](https://opensource.org/licenses/MIT). Feel free to

use, modify, and distribute the software according to the terms of this license.
