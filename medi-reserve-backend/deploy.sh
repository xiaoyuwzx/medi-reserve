#!/bin/bash
# =============================================
# MediReserve 一键部署脚本
# 用法: ./deploy.sh [start|stop|restart|status|logs|build|clean]
# =============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目名称
PROJECT_NAME="medi-reserve"
COMPOSE_FILE="docker-compose.yml"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_warn "docker-compose 命令未找到，尝试使用 docker compose"
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    log_info "使用命令: $COMPOSE_CMD"
}

# 检查环境变量文件
check_env() {
    if [ ! -f ".env" ]; then
        log_warn ".env 文件不存在，复制 .env.example 并配置"
        if [ -f ".env.example" ]; then
            cp .env.example .env
            log_info "已创建 .env 文件，请根据需要修改配置"
        else
            log_error ".env.example 文件不存在，请手动创建 .env 文件"
            exit 1
        fi
    fi
}

# 构建镜像
build() {
    log_info "开始构建 Docker 镜像..."
    check_env
    $COMPOSE_CMD -f $COMPOSE_FILE build --no-cache
    log_success "镜像构建完成"
}

# 启动服务
start() {
    log_info "启动 MediReserve 服务..."
    check_env

    # 创建必要的目录
    mkdir -p deploy/nginx/conf.d
    mkdir -p deploy/nginx/html
    mkdir -p deploy/mysql

    # 检查配置文件是否存在
    if [ ! -f "deploy/nginx/conf.d/medi-reserve.conf" ]; then
        log_warn "Nginx 配置文件不存在，请确保 deploy/nginx/conf.d/medi-reserve.conf 已创建"
    fi

    if [ ! -f "deploy/mysql/init.sql" ]; then
        log_warn "MySQL 初始化脚本不存在，请确保 deploy/mysql/init.sql 已创建"
    fi

    # 启动所有服务
    $COMPOSE_CMD -f $COMPOSE_FILE up -d

    log_success "所有服务已启动"

    # 等待服务就绪
    log_info "等待服务启动（约30秒）..."
    sleep 30

    # 显示状态
    status
}

# 停止服务
stop() {
    log_info "停止 MediReserve 服务..."
    $COMPOSE_CMD -f $COMPOSE_FILE down
    log_success "所有服务已停止"
}

# 重启服务
restart() {
    log_info "重启 MediReserve 服务..."
    stop
    start
}

# 查看状态
status() {
    log_info "服务状态："
    $COMPOSE_CMD -f $COMPOSE_FILE ps
    echo ""

    # 显示健康检查状态
    log_info "健康检查状态："
    for service in patient doctor admin websocket mysql redis nginx; do
        container_name="medi-$service"
        if docker ps -a --format '{{.Names}}' | grep -q "^$container_name$"; then
            health=$(docker inspect --format='{{.State.Health.Status}}' $container_name 2>/dev/null || echo "N/A")
            if [ "$health" = "healthy" ]; then
                echo -e "  $container_name: ${GREEN}healthy${NC}"
            elif [ "$health" = "unhealthy" ]; then
                echo -e "  $container_name: ${RED}unhealthy${NC}"
            elif [ "$health" = "N/A" ]; then
                echo -e "  $container_name: ${YELLOW}N/A${NC}"
            else
                echo -e "  $container_name: ${YELLOW}$health${NC}"
            fi
        else
            echo -e "  $container_name: ${RED}not running${NC}"
        fi
    done
}

# 查看日志
logs() {
    local service=$1
    if [ -z "$service" ]; then
        $COMPOSE_CMD -f $COMPOSE_FILE logs --tail=100 -f
    else
        $COMPOSE_CMD -f $COMPOSE_FILE logs --tail=100 -f $service
    fi
}

# 清理
clean() {
    log_warn "将删除所有容器、网络、卷和镜像"
    read -p "确认删除？(y/N): " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        $COMPOSE_CMD -f $COMPOSE_FILE down -v --rmi all
        log_success "清理完成"
    else
        log_info "取消清理"
    fi
}

# 帮助信息
help() {
    echo "MediReserve 部署脚本"
    echo ""
    echo "用法: $0 [COMMAND]"
    echo ""
    echo "命令:"
    echo "  build         构建 Docker 镜像"
    echo "  start         启动所有服务"
    echo "  stop          停止所有服务"
    echo "  restart       重启所有服务"
    echo "  status        查看服务状态"
    echo "  logs [服务名]  查看日志（可选指定服务）"
    echo "  clean         清理所有容器、卷和镜像"
    echo "  help          显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start"
    echo "  $0 logs patient"
    echo "  $0 status"
}

# ========== 主入口 ==========
main() {
    check_docker

    case "$1" in
        build)
            build
            ;;
        start)
            start
            ;;
        stop)
            stop
            ;;
        restart)
            restart
            ;;
        status)
            status
            ;;
        logs)
            logs "$2"
            ;;
        clean)
            clean
            ;;
        help|--help|-h)
            help
            ;;
        *)
            help
            ;;
    esac
}

# 执行主函数
main "$@"