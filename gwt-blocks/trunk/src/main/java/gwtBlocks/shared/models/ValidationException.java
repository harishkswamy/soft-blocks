// Copyright 2008 Harish Krishnaswamy
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package gwtBlocks.shared.models;

public class ValidationException extends RuntimeException
{
    private static final long  serialVersionUID = -3454227820042890585L;

    private FeedbackModel<?>[] _models;

    public ValidationException(String msg)
    {
        super(msg);
    }

    public ValidationException(FeedbackModel<?> model, String msg)
    {
        this(new FeedbackModel[] { model }, msg);
    }

    public ValidationException(FeedbackModel<?> model1, FeedbackModel<?> model2, String msg)
    {
        this(new FeedbackModel[] { model1, model2 }, msg);
    }

    public ValidationException(FeedbackModel<?> model1, FeedbackModel<?> model2, FeedbackModel<?> model3, String msg)
    {
        this(new FeedbackModel[] { model1, model2, model3 }, msg);
    }

    public ValidationException(FeedbackModel<?>[] models, String msg)
    {
        super(msg);
        _models = models;
    }

    public FeedbackModel<?>[] getMessageModels()
    {
        return _models;
    }
}
