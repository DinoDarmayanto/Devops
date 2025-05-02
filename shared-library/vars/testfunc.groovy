import id.co.bni.devops.Utils
def call () {
    def utils = new id.co.bni.devops.Utils()
    def agent = utils.changeAgent('java')

    pipeline {
        agent {
            node {
                label "${agent}"
            }
        }

        stages {
            stage('Hallo World') {
                steps {
                    script{
                        echo "Hello World"
                    }
                }
            }
        }
    }
}