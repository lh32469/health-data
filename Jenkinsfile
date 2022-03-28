// project should be the last token of the Git repo URL in lowercase.
def project = "health-data"
def branch = BRANCH_NAME.toLowerCase()
def port = "9020"
k8sYml = ""

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
          image 'maven:3.8-jdk-11'
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
          image 'maven:3.8-jdk-11'
          args '--dns=172.17.0.1 -u root \
                  -e PROJECT=watch \
                  -e BRANCH=junit \
                  -v /var/lib/jenkins/.m2:/root/.m2'
        }
      }
      steps {
        sh 'ls -l'
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


    stage('Templating') {
      steps {
        script {
          def file = readFile "k8s.yml"

          def binding = [
              project: project,
              branch : branch
          ]

          def engine = new groovy.text.SimpleTemplateEngine()
          def template = engine.createTemplate(file).make(binding)

          k8sYml = template.toString()
        }
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
            writeFile file: 'k8s-out.yml', text: k8sYml
            sh "kubectl -n ${namespace} create -f k8s-out.yml"
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
