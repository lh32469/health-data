def registry = "registry.container-registry:5000"
def dockerHost = "tcp://dind.container-registry:2375"

project = ""
branch = ""

pipeline {

  options {
    // Discard everything except the last 10 builds
    buildDiscarder(logRotator(numToKeepStr: '10'))
    // Don't build the same branch concurrently
    disableConcurrentBuilds()

    // Cleanup orphaned branch Kubernetes namespace
    branchTearDownExecutor 'Cleanup'
  }

  agent {
    kubernetes {
      yamlFile 'buildPod.yml'
    }
  }

  stages {

    stage('Setup') {
      steps {
        container('maven') {
          script {
            origin = sh(
                returnStdout: true,
                script: "git remote get-url origin"
            )
            project = origin.trim()
                .toLowerCase()
                .split("/")[1]
                .replaceAll(".git", "")
            branch = env.BRANCH_NAME.toLowerCase()
            println "Project/Branch = " + project + "/" + branch

            def file = readFile "k8s.yml"

            if (branch == "master") {
              host = project
            } else {
              host = "${branch}.${project}"
            }

            def binding = [
                project: project,
                branch : branch,
                host   : host
            ]

            def engine = new groovy.text.SimpleTemplateEngine()
            def template = engine.createTemplate(file).make(binding)

            k8sYml = template.toString()
          }
        }
      }
    }

    stage('Maven') {
      steps {
        container('maven') {
          sh 'mvn -B package'
        }
        junit '**/target/surefire-reports/TEST-*.xml'
      }
    }

    stage('Docker') {
      steps {
        container('docker') {
          sh 'docker -v'
          sh "docker build -t ${registry}/${project}:${branch} \
              --build-arg PROFILE=jenkins,${branch} \
              --label job.name=$JOB_NAME ."
          sh "docker push ${registry}/${project}:${branch}"
          sh 'docker image ls'
          // Cleanup image(s) once pushed
          sh "docker image prune -af \
              --filter label=job.name=$JOB_NAME"
          sh 'docker image ls'
        }
      }
    }

    stage('Kubernetes') {
      steps {
        container('kubectl') {

          script {
            def namespace = "${project}-${branch}" as String

            status = sh(
                returnStatus: true,
                script: "kubectl get namespace $namespace"
            )

            if (status == 0) {
              println "$namespace namespace exists"
              sh "kubectl -n ${namespace} rollout restart deployment/${project}"
            } else {
              sh "kubectl create namespace $namespace"
              writeFile file: 'k8s-out.yml', text: k8sYml
              sh "kubectl -n ${namespace} create -f k8s-out.yml"
            }

          }
        }
      }
    }

  }


}

