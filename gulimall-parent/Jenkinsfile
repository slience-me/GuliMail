pipeline {
  agent {
    node {
      label 'maven'
    }

  }
  stages {
    stage('拉取代码') {
      agent none
      steps {
        git(url: 'https://github.com/slience-me/gulimall-test-deploy.git', credentialsId: 'github-id', branch: 'master', changelog: true, poll: false)
        sh 'echo 正在构建 $PROJECT_NAME 版本号 $PROJECT_VERSION 将会提交给 $REGISTRY 镜像仓库'
        container('maven') {
          sh 'mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml'
        }

      }
    }

    stage('构建镜像 & 推送镜像') {
      steps {
        container('maven') {
          sh 'mvn -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml clean package'
          sh 'cd $PROJECT_NAME && docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
          withCredentials([usernamePassword(passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME', credentialsId: "$DOCKER_CREDENTIAL_ID")]) {
            sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
            sh 'docker push $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
          }

        }

      }
    }

    stage('推送最新版镜像') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {
          sh 'docker tag $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
          sh 'docker push $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
        }

      }
    }

    stage('部署到K8S') {
      agent none
      steps {
        input(id: 'deploy-to-dev-$PROJECT_NAME', message: '是否部署到集群中?')
        container('maven') {
          withCredentials([kubeconfigContent(credentialsId: 'demo-kubeconfig', variable: 'KUBECONFIG_CONTENT')]) {
            sh 'mkdir ~/.kube'
            sh 'echo "$KUBECONFIG_CONTENT" > ~/.kube/config'
            sh 'envsubst < $PROJECT_NAME/deploy/deploy.yaml | kubectl apply -f -'
          }

        }

      }
    }

    stage('发布版本') {
      when {
        expression {
          return params.PROJECT_VERSION =~ /v.*/
        }

      }
      steps {
        container('maven') {
          input(id: 'release-image-with-tag', message: '发布当前版本镜像吗?')
		  sh 'docker tag $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION'
          sh 'docker push $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION'
          withCredentials([usernamePassword(credentialsId: "$GITHUB_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
            sh 'git config --global user.email "slienceme.cn@gmail.com"'
            sh 'git config --global user.name "slience_me"'
            sh 'git tag -a $PROJECT_NAME-$PROJECT_VERSION -m "$PROJECT_VERSION"'
            sh 'git push https://$GIT_USERNAME:$GIT_PASSWORD@github.com/$GITHUB_ACCOUNT/gulimall-test-deploy.git --tags --ipv4'
          }
          
        }

      }
    }

  }
  environment {
    DOCKER_CREDENTIAL_ID = 'aliyun-hub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'registry.cn-beijing.aliyuncs.com'
    DOCKERHUB_NAMESPACE = 'slienceme-gulimall'
    GITHUB_ACCOUNT = 'slience-me'
    BRANCH_NAME = 'master'
  }
  parameters {
    string(name: 'PROJECT_VERSION', defaultValue: 'v0.0Beta', description: '版本号')
    string(name: 'PROJECT_NAME', defaultValue: 'gulimall-gateway', description: '构建模块')
  }
}