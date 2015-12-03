class Line
{
   public:
      void setLength( double len );
      double getLength( void );
      Line();

   private:
      double length;
} rect;


char history[10][MAX_LINE];
int numCommands;

//Adds to history
void addToHistory(char *line) {
	int i = 0;
	for(i; i < 9; i++) {
		strcpy(history[i],history[i+1]);
	}

	//not an empty line add
	if(line[0]) {
		strcpy(history[9],line);
		numCommands++;
	}
}


//executes command
//if & run in background
void executeCommand(char* arg,char* args[],char lastChar) {
	pid_t pid = fork();
	//child process
	if(pid == 0){
		execvp(args[0],args);
		exit(0);
	}else if(pid > 0) {
		if(lastChar != '&') {
			wait(NULL);
		}
	}
}

//prints a list of history
void printHistory() {
	int currCommand = numCommands;
	int i = 9;
	int j = 0;
	char* c;
	for(i; i >= 0; i--) {
		if(currCommand > 0) {
			printf("%d ",currCommand);
			for(j = 0; history[i][j] != '\0';j++) {
				printf("%c",history[i][j]);
			}
			printf("\n");
			fflush(stdout);
			currCommand--;
		}
	}
}

void parseLine(char *line,char *lineCopy, char *args[],char *lastChar) {
		int argsIndex = 0;
		strcpy(lineCopy,line);
		char* token = strtok(line," ");
		while(token) {
			args[argsIndex] = token;
			argsIndex++;
			token = strtok(NULL," ");
		}

		args[argsIndex] = NULL;

		//grab the last character
		if(argsIndex != 0) {
			int lastWordIndex = argsIndex - 1;
			int len = strlen(args[lastWordIndex]);
			*lastChar = args[lastWordIndex][len -1];
			//ignore word in execution if &
			if(*lastChar == '&') {
				args[lastWordIndex] = NULL;
			}
		}
}

int main(void)
{
	int should_run = 1;
	int argsIndex = 0;
	numCommands = 0;

	char *args[MAX_LINE/2 + 1];
	char line[MAX_LINE];
	char lineCopy[MAX_LINE];
	char *lastChar;

	while (should_run) {
		*lastChar = ' ';
		argsIndex = 0;
		fflush(stdout);
		printf("osh>");
		fflush(stdout);
		//string reading code
		gets(line);

		//get params from line
		parseLine(line,lineCopy,args,lastChar);

		//command execution code
		if(args[0]) {
			//repeat last command
			if(strcmp("!!",args[0]) == 0) {

				if(numCommands == 0) {
					printf("No commands in history \n");
					fflush(stdout);
				}else {
					strcpy(line,history[9]);
					parseLine(line,lineCopy,args,lastChar);
					fflush(stdout);
					executeCommand(args[0],args,*lastChar);
					fflush(stdout);
					addToHistory(lineCopy);
				}
			//execute given command
			} else if(args[0][1] && args[0][0] == '!') {
				args[0]++;
				int command = atoi(args[0]);
 				int commandIndex =  command - numCommands + 9;
 				if(commandIndex > 9 || commandIndex < 0){
 					printf("No such command in history \n");
 					fflush(stdout);
 				}else{
	 				strcpy(line,history[commandIndex]);
					parseLine(line,lineCopy,args,lastChar);
					fflush(stdout);
					executeCommand(args[0],args,*lastChar);
					fflush(stdout);
					addToHistory(lineCopy);
				}
			//exit command
			} else if(strcmp("exit",args[0]) == 0){
				should_run = 0;
			//print history command
			} else if(strcmp("history",args[0]) == 0) {
				fflush(stdout);
				printHistory();
				fflush(stdout);
			//exec command
			} else {
				fflush(stdout);
				executeCommand(args[0],args,*lastChar);
				fflush(stdout);
				addToHistory(lineCopy);
			}
		}
	}
}
