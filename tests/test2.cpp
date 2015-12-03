#include <stdio.h>
#include <pthread.h>
#include <semaphore.h>
#include <time.h>
#include <stdlib.h>
#include <stdbool.h>

#define NUM_CHAIRS 3
#define NUM_STUDENTS 5
#define NUM_TA_CHAIRS 1



sem_t taSemaphore;
sem_t taChairSemaphore;
sem_t workingWithTaSemaphore;

pthread_mutex_t numFreeChairsMutex;
int numFreeChairs;
bool taAsleep;

void *simulateStudent();
void *simulateTA();

int main(int argc, char *argv[]) {

    //initialize variables from constants
    int numStudents = NUM_STUDENTS;
    int numChairs = NUM_CHAIRS;
    int numTAChairs = NUM_TA_CHAIRS;
    numFreeChairs = NUM_CHAIRS;
    taAsleep = false;

    //seed random function
    srand(time(NULL));

    //set numStudets to arg if provided
    if(argc > 1) {
        numStudents = atoi(argv[1]);
    }

    pthread_t taThread;
    pthread_t studentThreads[numStudents];

    //initialize semaphores
    sem_init(&taSemaphore,0,0);
    sem_init(&workingWithTaSemaphore,0,0);
    sem_init(&taChairSemaphore,0,numTAChairs);

    pthread_mutex_init(&numFreeChairsMutex,NULL);

    //Initialize TA thread
    printf("Initializing TA thread\n");
    pthread_create(&taThread,NULL,simulateTA,NULL);

    //Initialize student threads
    int i = 0;
    printf("Initializing %d student threads \n",numStudents);
    for(i = 0; i < numStudents; i++) {
        pthread_create(&studentThreads[i],NULL,simulateStudent,NULL);
    }

    for(i = 0; i < numStudents; i++) {
        pthread_join(studentThreads[i],NULL);
    }
    pthread_join(taThread,NULL);


}

void *simulateStudent() {
    while(1) {
        //generate random number between 1 and 45
        //duration is time spent programming
        int duration = rand() % 45 +1;
        sleep(duration);

        //Student goes to TA's office and checks whether a seat is available
        pthread_mutex_lock(&numFreeChairsMutex);
        if(numFreeChairs > 0) {
            numFreeChairs--;
            printf("Student arrives at TA's office... \n");
        }else {
            printf("Student arrives at TA's office... There are no more chairs left! Student returns to programming...\n");
            pthread_mutex_unlock(&numFreeChairsMutex);
            continue;
        }
        pthread_mutex_unlock(&numFreeChairsMutex);

        //student is waiting outside until TA is available
        sem_wait(&taChairSemaphore);

        //Enter office and wake up TA
        sem_post(&taSemaphore);

        //Wait until finished with TA
        sem_wait(&workingWithTaSemaphore);

        //Finished with TA, tell next student to enter
        sem_post(&taChairSemaphore);
    }
}

void *simulateTA() {
    while(1) {
        //TA goes back to sleep if no one is outside
        pthread_mutex_lock(&numFreeChairsMutex);
        if(numFreeChairs == NUM_CHAIRS) {
            taAsleep = true;
            printf("TA is going back to sleep...\n");
        }
        pthread_mutex_unlock(&numFreeChairsMutex);

        sem_wait(&taSemaphore);

        //TA lets in next available student
        pthread_mutex_lock(&numFreeChairsMutex);
        numFreeChairs++;
        printf("Student enters TA's Office...\n");
        pthread_mutex_unlock(&numFreeChairsMutex);

        if(taAsleep) {
            taAsleep = !taAsleep;
            printf("TA is awoken by student...\n");
        }

        //Student woke TA up
        //work with student for random duration
        int duration = rand() % 15 + 1;
        printf("TA is helping student for %d seconds... \n",duration);
        sleep(duration);
        //finished working release student
        printf("Student leaves TA's Office...\n");

        sem_post(&workingWithTaSemaphore);
    }
}
