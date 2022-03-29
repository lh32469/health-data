k8sYml = ""
project = ""
branch = ""

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

    stage("Get project") {
      steps {
        script {
          // project is the last token of the Git repo URL in lowercase.
          project = env.GIT_URL.toLowerCase().split("/")[1].replaceAll(".git", "")
          branch = BRANCH_NAME.toLowerCase()
          println "Project/Branch = " + project + "/" + branch
        }
      }
    }

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
        // Delete image once pushed
        sh "docker image rm dell-0114.local:32000/${project}:${branch}"
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

          if ("kubectl get namespace ${namespace}".execute().waitFor() == 0) {
            println "$namespace namespace exists"
            sh "kubectl -n ${namespace} rollout restart deployment/${project}"
          } else {
            "kubectl create namespace $namespace".execute().waitFor()
            writeFile file: 'k8s-out.yml', text: k8sYml
            sh "kubectl -n ${namespace} create -f k8s-out.yml"
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
