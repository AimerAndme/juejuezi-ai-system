"""
自定义异常类
对应Java版本的异常处理
"""

from typing import Any, Optional
from fastapi import HTTPException, status


class BaseAPIException(HTTPException):
    """基础API异常类"""
    
    def __init__(
        self,
        status_code: int = status.HTTP_500_INTERNAL_SERVER_ERROR,
        code: int = 500,
        message: str = "服务器内部错误",
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        self.code = code
        self.message = message
        self.data = data
        super().__init__(status_code=status_code, detail=message, headers=headers)
    
    def to_dict(self) -> dict:
        """转换为字典格式（用于响应）"""
        return {
            "code": self.code,
            "message": self.message,
            "data": self.data,
        }


class BusinessException(BaseAPIException):
    """业务异常"""
    
    def __init__(
        self,
        message: str = "业务异常",
        code: int = 400,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(
            status_code=status.HTTP_400_BAD_REQUEST,
            code=code,
            message=message,
            data=data,
            headers=headers,
        )


class AuthenticationException(BaseAPIException):
    """认证异常"""
    
    def __init__(
        self,
        message: str = "认证失败",
        code: int = 401,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(
            status_code=status.HTTP_401_UNAUTHORIZED,
            code=code,
            message=message,
            data=data,
            headers=headers,
        )


class AuthorizationException(BaseAPIException):
    """授权异常"""
    
    def __init__(
        self,
        message: str = "权限不足",
        code: int = 403,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(
            status_code=status.HTTP_403_FORBIDDEN,
            code=code,
            message=message,
            data=data,
            headers=headers,
        )


class ResourceNotFoundException(BaseAPIException):
    """资源未找到异常"""
    
    def __init__(
        self,
        message: str = "资源未找到",
        code: int = 404,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            code=code,
            message=message,
            data=data,
            headers=headers,
        )


class ValidationException(BaseAPIException):
    """验证异常"""
    
    def __init__(
        self,
        message: str = "参数验证失败",
        code: int = 422,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            code=code,
            message=message,
            data=data,
            headers=headers,
        )


class RateLimitException(BaseAPIException):
    """限流异常"""
    
    def __init__(
        self,
        message: str = "请求过于频繁",
        code: int = 429,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            code=code,
            message=message,
            data=data,
            headers=headers,
        )


class FileException(BaseAPIException):
    """文件相关异常"""
    
    def __init__(
        self,
        message: str = "文件操作失败",
        code: int = 500,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            code=code,
            message=message,
            data=data,
            headers=headers,
        )


class FileContentException(FileException):
    """文件内容异常"""
    
    def __init__(
        self,
        message: str = "文件内容异常",
        code: int = 400,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(message=message, code=code, data=data, headers=headers)


class FileFormatException(FileException):
    """文件格式异常"""
    
    def __init__(
        self,
        message: str = "文件格式不支持",
        code: int = 400,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(message=message, code=code, data=data, headers=headers)


class FileParseException(FileException):
    """文件解析异常"""
    
    def __init__(
        self,
        message: str = "文件解析失败",
        code: int = 500,
        data: Any = None,
        headers: Optional[dict] = None,
    ):
        super().__init__(message=message, code=code, data=data, headers=headers)


# 错误码定义（对应Java版本的ErrorCode）
class ErrorCode:
    """错误码常量"""
    
    # 通用错误
    SUCCESS = 200
    BAD_REQUEST = 400
    UNAUTHORIZED = 401
    FORBIDDEN = 403
    NOT_FOUND = 404
    INTERNAL_ERROR = 500
    
    # 业务错误
    BUSINESS_ERROR = 1000
    VALIDATION_ERROR = 1001
    RESOURCE_NOT_FOUND = 1002
    
    # 文件相关错误
    FILE_NOT_EXIST = 2001
    FILE_FORMAT_ERROR = 2002
    FILE_PARSE_ERROR = 2003
    FILE_CONTENT_ERROR = 2004
    
    # 用户相关错误
    USER_NOT_FOUND = 3001
    USER_AUTH_FAILED = 3002
    USER_QUOTA_EXCEEDED = 3003
    
    # AI相关错误
    AI_SERVICE_ERROR = 4001
    AI_MODEL_ERROR = 4002
    AI_RATE_LIMIT = 4003
    
    # 数据库相关错误
    DATABASE_ERROR = 5001
    DATABASE_CONNECTION_ERROR = 5002