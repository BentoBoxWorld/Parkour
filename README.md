# Parkour Addon
<img width="403" alt="parkour" src="https://github.com/BentoBoxWorld/Parkour/assets/4407265/10dcc201-9686-4b3a-b9c4-e5fd52b79b0d">

## Description
The Parkour addon is a BentoBox addon for Minecraft that allows players to create and play parkour courses within the game. It provides a creative mode for course creation and a survival mode for course gameplay. The plugin features gold pressure plates as the starting and ending points of the courses. It tracks the time taken by players to complete the courses and maintains a scores for each course. Additionally, creators can place checkpoints along their courses to help players restart from the last checkpoint if they fall. The plugin also supports team gameplay, allowing players to form teams and collaborate on course creation using the same engine as BSkyBlock. Players can expose their course using the `/pk setwarp` command and explore and compete in other courses using the `/pk courses` command.

## Features
- Course Creation: Players can create parkour courses in creative mode using the plugin.
- Course Gameplay: Players can play the created courses in survival mode.
- Start and End Points: Courses start and end with a gold pressure plate, providing clear markers.
- Time Tracking: The plugin measures the time taken by players to complete each course.
- High Score: A high score is maintained for each course, allowing players to compete for the best time.
- Checkpoints: Creators can place checkpoints along their courses to assist players in restarting from the last checkpoint if they fall.
- Team Collaboration: The plugin uses the same engine as BSkyBlock, enabling players to form teams and work together on course creation.
- Course Teleportation: The `/pk courses` command lists all the tracks or courses made, allowing players to teleport to them.
- Competition: Players can compete against each other to achieve the best time on the courses.

## Usage
To get started, use the `/pk` or `/parkour` command to access the parkour plugin. This will teleport players to an example parkour course and get them started. Admins can use blueprints to change the default course example or make additional ones.

### Course Creation
1. Enter creative mode. Players will need permission to do this.
2. Use creative mode tools to design your parkour course.
3. Place gold pressure plates at the start and end points of the course.
4. Optionally, add checkpoints throughout the course using blackstone pressure plates.
5. Test your course to ensure it is playable and challenging.

### Course Gameplay
1. Locate the course you want to play using the `/pk courses` command, or play your own.
2. Click on one to reach the course.
3. Step on the gold pressure plate to start the course. You will enter survival mode.
4. Complete the course as quickly as possible, avoiding falls and reaching each checkpoint if available.
5. Step on the gold pressure plate at the end to finish the course and record your time.

### Team Collaboration
1. Form a team with other players using the team creation commands `pk team invite <player name>`.
2. Collaborate with your team members to design and build parkour courses.
3. When you want to make your course available go to the starting area and set a warp point for visitors using `/pk setwarp`
4. Use the `/pk courses` command to view and play each other's courses.
5. Compete within the team to achieve the best times on the courses.

## Commands
- `/pk`: Main command to start a parkour course and teleport to the parkour world.
- `/pk courses`: List all the tracks or courses available and teleport to them.
- `/pk top`: Show the Top Ten.
- `/pk setwarp`: Places the course in the courses menu and sets the warp position visitors will arrive at
- `/pk removewarp`: Makes the course private and removes the visitor's warp

## Permissions

See [addon.yml](https://github.com/BentoBoxWorld/Parkour/blob/aeb01c8ef8ca1c07d9dde570e7ab61201710c6d5/src/main/resources/addon.yml#L11) for a full list.

## Compatibility
The Parkour plugin is compatible with Minecraft versions 1.20.1 and requires BentoBox 1.24.0 or later.

## Troubleshooting
If you encounter any issues or have questions regarding the Parkour plugin, please reach out to our support team for assistance.

## License
The Parkour plugin is released under the [Eclipse Public License 2.0](https://github.com/BentoBoxWorld/Parkour/blob/develop/LICENSE).
