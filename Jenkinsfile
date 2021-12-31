// project should be the last token of the Git repo URL in lowercase.
def project = "watch"
def branch = BRANCH_NAME.toLowerCase()
def port = "9020"

pipeline {

  options {
    // Discard everything except the last 10 builds
    buildDiscarder(logRotator(numToKeepStr: '10'))
    // Don't build the same branch concurrently
    disableConcurrentBuilds()

    // Cleanup orphaned branch Kubernetes namespace
    branchTearDownExecutor 'CleanupKubernetes'
  }

  agent any

  stages {

    stage('Compile') {
      agent {
        docker {
          reuseNode true
          image 'maven:latest'
          args '-u root -v /var/lib/jenkins/.m2:/root/.m2'
        }
      }
      steps {
        sh 'mvn -B -DskipTests clean compile'
      }
    }

    stage('Test') {
      agent {
        docker {
          reuseNode true
          image 'maven:latest'
          args '--dns=172.17.0.1 -u root \
                  -e PROJ=watch \
                  -e BRNCH=junit \
                  -v /var/lib/jenkins/.m2:/root/.m2'
        }
      }
      steps {
        sh 'mvn -B package'
        junit '**/target/surefire-reports/TEST-*.xml'
      }
    }


    stage('Docker Image') {
      steps {
        // Cleanup previous images
        sh "docker image prune -af \
              --filter label=job.name=$JOB_NAME"
        sh "docker build -t dell-0114.local:32000/${project}:${branch} \
              --build-arg PROFILE=jenkins,${branch} \
              --label job.name=$JOB_NAME ."
        sh "docker push dell-0114.local:32000/${project}:${branch}"
      }
    }

    stage('Deploy') {
      steps {
        script {

          def namespace = "${project}-${branch}" as String

          result = sh(
              returnStdout: true,
              script: "kubectl get namespaces | grep $namespace || true"
          )

          if (result.trim().isEmpty()) {
            sh "kubectl create namespace $namespace"
              sh "sed s/PROJECT/$project/ k8s.yml | \
                  sed s/BRANCH/$branch/ | \
                  kubectl -n ${namespace} create -f -"
          } else {
            echo "$namespace namespace exists"
            sh "kubectl -n ${namespace} rollout restart deployment/${project}"
          }

        }
      }
    }

  }

  post {
    always {
      // Cleanup Jenkins workspace
      cleanWs()
    }
  }

}
